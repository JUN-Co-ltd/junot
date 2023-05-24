package jp.co.jun.edi.component;

import java.io.File;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Optional;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.entity.TDeliveryFileInfoEntity;
import jp.co.jun.edi.entity.TFileEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TDeliveryFileInfoRepository;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TFileRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;

/**
 * PDFファイルを作成するコンポーネント.
 */
@Component
public class DeliveryRequestCreatePdfComponent {

    private static final String CONTENT_TYPE_PDF = "application/pdf";

    private static final String PDF_FILENAME_TYPE_NI = "NI";

    @Autowired
    private S3Component s3Component;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TDeliveryFileInfoRepository tDeliveryFileInfoRepository;
    @Autowired
    private TFileRepository tFileRepository;
    @Autowired
    private TDeliveryRepository tDeliveryRepository;

    /**
     * PDFファイルを作成する.
     * @param userId ユーザID
     * @param deliveryId 納品ID
     * @param xslPath XSLファイルパス
     * @param xmlPath XMLファイルパス
     * @param pdfPath PDFファイルパス
     * @return PDFファイル名
     * @throws Exception Exception
     */
    public String createPdf(final BigInteger userId, final BigInteger deliveryId, final Path xslPath, final Path xmlPath, final Path pdfPath)
            throws Exception {

        final TDeliveryEntity tDeliveryEntity = getDelivery(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResultMessages.warning().add(
                                MessageCodeType.CODE_002, LogStringUtil.of("createPdf")
                                        .message("t_delivery not found.")
                                        .value("delivery_id", deliveryId)
                                        .build())));

        // PDFファイルを作成
        genaratedPdf(xslPath, xmlPath, pdfPath);

        // S3アップロード
        final String s3Key = s3Component.upload(pdfPath.toFile(), propertyComponent.getCommonProperty().getS3PrefixPdf(), CONTENT_TYPE_PDF);

        // ファイル情報を登録
        final TFileEntity tFileEntity = registTFile(s3Key, userId, tDeliveryEntity);

        // 納品ファイル情報を登録
        registDelieryFileInfo(tDeliveryEntity, tFileEntity, userId);

        return tFileEntity.getFileName();

    }

    /**
     * PDFを生成する.
     * @param xslPath XSLファイルパス
     * @param xmlPath XMLファイル
     * @param pdfPath PDFファイル
     * @throws Exception Exception
     */
    public void genaratedPdf(final Path xslPath, final Path xmlPath, final Path pdfPath) throws Exception {
        // FOPの変換をする
        final FopFactory fopFactory = FopFactory.newInstance(new File(propertyComponent.getBatchProperty().getScheduleFopPathXconf()));
        final FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        // Setup output
        try (OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(pdfPath.toFile()))) {
            // Construct fop with desired output format
            final Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            // Setup XSLT
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer transformer = factory.newTransformer(new StreamSource(xslPath.toFile()));

            // Set the value of a <param> in the stylesheet
            //transformer.setParameter("versionParam", "2.0");

            // Setup input for XSLT transformation
            final Source src = new StreamSource(xmlPath.toFile());
            // Resulting SAX events (the generated FO) must be piped through to FOP
            final Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);
        }
    }

    /**
     * 発注ファイル情報登録.
     * @param tDeliveryEntity 納品情報
     * @param tFileEntity ファイル情報
     * @param userId ユーザID
     * @return TOrderFileInfoEntity
     * @throws Exception 例外
     */
    private TDeliveryFileInfoEntity registDelieryFileInfo(final TDeliveryEntity tDeliveryEntity, final TFileEntity tFileEntity, final BigInteger userId)
            throws Exception {
        final TDeliveryFileInfoEntity tDeliveryFileInfoEntity = new TDeliveryFileInfoEntity();
        tDeliveryFileInfoEntity.setId(null);
        tDeliveryFileInfoEntity.setDeliveryId(tDeliveryEntity.getId());
        tDeliveryFileInfoEntity.setDeliveryCount(tDeliveryEntity.getDeliveryCount());
        tDeliveryFileInfoEntity.setOrderId(tDeliveryEntity.getOrderId());
        tDeliveryFileInfoEntity.setFileNoId(tFileEntity.getId());
        tDeliveryFileInfoEntity.setPublishedAt(DateUtils.createNow());
        tDeliveryFileInfoEntity.setPublishedEndAt(null);
        tDeliveryFileInfoEntity.setUpdatedUserId(userId);
        tDeliveryFileInfoEntity.setCreatedUserId(userId);
        tDeliveryFileInfoRepository.save(tDeliveryFileInfoEntity);
        return tDeliveryFileInfoEntity;
    }

    /**
     * ファイル情報登録.
     * @param s3Key S3キー
     * @param userId ユーザID
     * @param tDeliveryEntity 納品情報
     * @return TFileEntity
     * @throws Exception 例外
     */
    private TFileEntity registTFile(final String s3Key, final BigInteger userId, final TDeliveryEntity tDeliveryEntity) throws Exception {

        // ファイル名：品番CHAR(8)-種別CHAR(2)発注番号CHAR(8)回数CHAR(2).pdf
        // 回数：2桁に満たない場合はゼロ(0)埋め
        final String fileName = String.format("%s-%s%s%02d.pdf", tDeliveryEntity.getPartNo(), PDF_FILENAME_TYPE_NI, tDeliveryEntity.getOrderNumber(),
                tDeliveryEntity.getDeliveryCount());

        final TFileEntity tFileEntity = new TFileEntity();
        tFileEntity.setId(null);
        tFileEntity.setContentType(CONTENT_TYPE_PDF);
        tFileEntity.setFileName(fileName);
        tFileEntity.setS3Key(s3Key);
        tFileEntity.setS3Prefix(propertyComponent.getCommonProperty().getS3PrefixPdf());
        tFileEntity.setUpdatedUserId(userId);
        tFileEntity.setCreatedUserId(userId);
        tFileRepository.save(tFileEntity);
        return tFileEntity;
    }

    /**
     * 処理前の納品依頼メール送信のヘッダー情報 を取得する.
     * @param deliveryId 納品ID
     * @return 処理前の納品依頼メール送信のヘッダー情報のリスト
     * @throws Exception 例外
     */
    public Optional<TDeliveryEntity> getDelivery(final BigInteger deliveryId) throws Exception {
        return tDeliveryRepository.findById(deliveryId);
    }

}
