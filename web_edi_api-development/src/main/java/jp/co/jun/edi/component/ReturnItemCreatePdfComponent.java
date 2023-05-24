package jp.co.jun.edi.component;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Path;

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

import jp.co.jun.edi.entity.TFileEntity;
import jp.co.jun.edi.entity.TMakerReturnFileInfoEntity;
import jp.co.jun.edi.repository.TFileRepository;
import jp.co.jun.edi.repository.TMakerReturnFileInfoRepository;

/**
 * PDFファイルを生成する.
 */
@Component
public class ReturnItemCreatePdfComponent {
    /** PDFファイル.コンテンツタイプ(PDF). */
    private static final String CONTENT_TYPE_PDF = "application/pdf";

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private S3Component s3Component;

    @Autowired
    private TFileRepository tFileRepository;

    @Autowired
    private TMakerReturnFileInfoRepository tMakerReturnFileInfoRepository;

    /**
     * PDF作成.
     * @param userId ユーザID
     * @param vouNum 伝票番号
     * @param orderId 発注ID
     * @param xslPath XSLファイルパス
     * @param xmlPath XMLファイルパス
     * @param pdfPath PDFファイルパス
     * @param fileName PDFのファイル名
     * @throws Exception 例外
     */
    public void createPdf(final BigInteger userId, final String vouNum, final BigInteger orderId, final Path xslPath, final Path xmlPath, final Path pdfPath,
             final String fileName)
            throws Exception {

        // PDFファイル生成
        genaratedPdf(xslPath, xmlPath, pdfPath);

        // S3アップロード
        final String s3Key = s3Component.upload(pdfPath.toFile(), propertyComponent.getCommonProperty().getS3PrefixPdf(), CONTENT_TYPE_PDF);

        // ファイル情報を登録
        final TFileEntity tFileEntity = registTFile(s3Key, userId, fileName);

        // メーカー返品ファイル情報を登録
        registFileInfo(orderId, vouNum, tFileEntity, userId);
    }

    /**
     * PDFファイルを生成し、一時フォルダに出力する.
     * @param xslPath XSLファイルパス
     * @param xmlPath XMLファイルパス
     * @param pdfPath PDFファイルパス
     * @throws IOException 例外
     * @throws Exception 例外
     */
    private void genaratedPdf(final Path xslPath, final Path xmlPath, final Path pdfPath) throws IOException, Exception {
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

            // Setup input for XSLT transformation
            final Source src = new StreamSource(xmlPath.toFile());

            // Resulting SAX events (the generated FO) must be piped through to FOP
            final Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);

        }
    }

    /**
     * ファイル情報登録.
     * @param s3Key S3キー
     * @param userId ユーザID
     * @param fileName ファイル名
     * @return TFileEntity
     */
    private TFileEntity registTFile(final String s3Key, final BigInteger userId, final String fileName) {
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
     * メーカー返品ファイル情報を登録.
     * @param orderId 発注ID
     * @param vouNum 伝票番号
     * @param tFileEntity ファイル情報
     * @param userId ユーザID
     * @return TOrderFileInfoEntity
     */
    private TMakerReturnFileInfoEntity registFileInfo(final BigInteger orderId, final String vouNum, final TFileEntity tFileEntity, final BigInteger userId) {
        final TMakerReturnFileInfoEntity tMakerReturnFileInfoEntity = new TMakerReturnFileInfoEntity();
        tMakerReturnFileInfoEntity.setId(null);
        tMakerReturnFileInfoEntity.setFileNoId(tFileEntity.getId());
        tMakerReturnFileInfoEntity.setOrderId(orderId);
        tMakerReturnFileInfoEntity.setVoucherNumber(vouNum);
        tMakerReturnFileInfoEntity.setUpdatedUserId(userId);
        tMakerReturnFileInfoEntity.setCreatedUserId(userId);
        tMakerReturnFileInfoRepository.save(tMakerReturnFileInfoEntity);
        return tMakerReturnFileInfoEntity;
    }

}
