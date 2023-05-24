//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.icu.text.SimpleDateFormat;

import jp.co.jun.edi.entity.TFileEntity;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.model.FileModel;
import jp.co.jun.edi.model.PurchaseRecordSearchConditionModel;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PurchaseRecordCreatePdfComponent {
	private static final String TEMP_DIRECTORY = "PurchaseRecords";
	private static final String VOUCHER_NAME = "PurchaseRecords";
	// PRD_0151 del JFE start
	//リリースする時はコメントを切り替える。ローカルではなくする。
	//private static final String XSL_PATH = "/opt/junot-api/config/pdf/xsl/purchase-record.xsl";
	//private static final String XSL_PATH = "C:/Users/KIT76807/Desktop/JUN/web_edi_api-staging/config/pdf/xsl/purchase-record.xsl";
	// PRD_0151 del JFE end
	@Autowired
	private PropertyComponent propertyComponent;

	@Autowired
	private PurchaseRecordCreateXmlComponent createXmlComponent;

    final FileModel fileModel = new FileModel();

	/**
	     * PDF作成処理の実行.
	     *
	     * @param entity 伝票管理情報
	 * @return
	 * @return
	     */
	    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)

	    public FileModel createPDF(final PurchaseRecordSearchConditionModel model) {
        log.info(LogStringUtil.of("execute")
                .message("Start processing of PurchaseRecordCreatePDF.")
                .build());
        try {
            // プロパティ情報取得
            // 一時フォルダ
            final String temporayFolder = getTemporaryPath(TEMP_DIRECTORY);

            // XMLファイルパス
            final Path xmlPath =
                    generatedFilePath(temporayFolder,
                            VOUCHER_NAME,
                            ".xml");

            // PDFファイルパス
            final Path pdfPath =
                    generatedFilePath(temporayFolder,
                            VOUCHER_NAME,
                            ".pdf");

            // XSLファイルパス
            // PRD_0151 mod JFE start
//            final Path xslPath = getXslPath(
//            		XSL_PATH,
//                    VOUCHER_NAME);
            String tmpDirectory = propertyComponent.getBatchProperty().getPurchaseRecordPathXsl();
            final Path xslPath = getXslPath(
            		tmpDirectory,
                    VOUCHER_NAME);
            // PRD_0151 mod JFE end
            // PDFファイル名(一時ファイル→正式なファイル名に変更)
            final String fileName = generatedPDFFileName();

            // XML作成
            createXmlComponent.createXml(
                    xmlPath,
                    model);

            // PDF作成
            createPdf(xslPath, xmlPath, pdfPath, fileName);

            //PDFをバイナリに。
            TFileEntity fileEntity = new TFileEntity();
            File file = new File(pdfPath.toString());

            byte[] fileData;
            FileInputStream fs = new FileInputStream(file);
            fileData =  IOUtils.toByteArray(fs);
            fileEntity.setContentType("application/pdf");
            fileEntity.setFileData(fileData);
            fileEntity.setFileName(fileName);

            setEntityDataToModel(fileEntity, fileModel);

            log.info(LogStringUtil.of("execute")
                    .message("End processing of PurchaseRecordCreatePDF.")
                    .build());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
		return fileModel;
    }


	/**
	 * PDFファイル名 生成.
	 *
	 * @param deliveryId 納品ID
	 * @param deliveryCount 納品依頼回数
	 * @param orderId 発注ID
	 * @return PDF名
	 */
	private String generatedPDFFileName() {
		Date nowdate = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String d = format.format(nowdate);

		// ファイル名：品番-発注番号CHAR(6)-回数-納品番号CHAR(6)-PK.pdf
		final String fileName = String.format("PurchaseRecord%s.pdf",
				d);
		return fileName;
	}

	/**
	 * 一時ディレクトリを取得する.
	 *
	 * ディレクトリ構成：{一時ディレクトリ}/{伝票名}/{ユーザID}/
	 * @param userId ユーザID
	 * @param voucherDirectoryName 伝票名
	 * @return 発注別の一時ディレクトリ
	 * @throws Exception 例外
	 */
	protected String getTemporaryPath(final String voucherDirectoryName) throws Exception {
		String tmpDirectory = propertyComponent.getBatchProperty().getScheduleFopTemporaryFolder();
		if (!tmpDirectory.endsWith("/")) {
			// 接尾辞がスラッシュでない場合、接尾辞にスラッシュをつける
			tmpDirectory = tmpDirectory.concat("/");
		}
		tmpDirectory = tmpDirectory.concat(voucherDirectoryName + "/");
		Files.createDirectories(Paths.get(tmpDirectory));

		return tmpDirectory;
	}

	/**
	 * ファイルパスを生成.
	 *
	 * @param path 一時フォルダ
	 * @param voucherName 伝票名
	 * @param voucherCategory 伝票タイプ
	 * @param orderId 発注ID
	 * @param extension 拡張子
	 * @return ファイルパス
	 */
	protected Path generatedFilePath(
			final String path,
			final String voucherName,
			final String extension) {
		// XMLファイル {伝票名}{伝票番号}{発注ID}_{yyyyMMddHHmmssSSS}.xml (or .pdf)
		final String nowDate = DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssSSS");
		final String filePath = voucherName + "_" + nowDate + extension;

		return Paths.get(path + filePath);
	}

	/**
	 * XSLファイルパスを取得.
	 *
	 * @param strXslPath XSLファイル指定プロパティ情報
	 * @param voucherName 伝票名
	 * @return XSLファイルパス
	 * @throws Exception 例外
	 */
	protected Path getXslPath(final String strXslPath, final String voucherName) throws Exception {
		final Path xslPath = Paths.get(strXslPath);
		if (!Files.exists(xslPath)) {
			throw new ScheduleException(LogStringUtil.of("getXslPath")
					.message("not found path.")
					.value(VOUCHER_NAME + ".path.xsl", strXslPath)
					.build());
		}
		return xslPath;
	}

	/**
	 * PDF作成し、ファイル情報に登録する.
	 *
	 * @param xslPath XSLファイルパス
	 * @param xmlPath XMLファイルパス
	 * @param pdfPath PDFファイルパス
	 * @param userId ユーザID
	 * @param fileName PDFのファイル名
	 * @throws Exception 例外
	 */
	public void createPdf(final Path xslPath, final Path xmlPath, final Path pdfPath,
			final String fileName) throws Exception {
		// PDFファイル生成
		genaratedPdf(xslPath, xmlPath, pdfPath);

		// S3アップロード
		//final String s3Key = s3Component.upload(pdfPath.toFile(), propertyComponent.getCommonProperty().getS3PrefixPdf(), CONTENT_TYPE_PDF);

		// ファイル情報を登録
		//final TFileEntity tFileEntity = registTFile(s3Key, userId, fileName);

		//return tFileEntity.getId();
	}

	/**
	 * PDFファイルを生成し、一時フォルダに出力する.
	 *
	 * @param xslPath XSLファイルパス
	 * @param xmlPath XMLファイルパス
	 * @param pdfPath PDFファイルパス
	 * @throws IOException 例外
	 * @throws Exception 例外
	 */
	private void genaratedPdf(final Path xslPath, final Path xmlPath, final Path pdfPath)
			throws IOException, Exception {
		// FOPの変換をする
		final FopFactory fopFactory = FopFactory.newInstance(
				new File(propertyComponent.getBatchProperty().getScheduleFopPathXconf()));
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
     * ModelにEntityの値を設定する.
     * @param fileEntity {@link TFileEntity} instance
     * @param fileModel {@link FileModel} instance
     */
    private void setEntityDataToModel(final TFileEntity fileEntity, final FileModel fileModel) {
        fileModel.setContentType(fileEntity.getContentType());
        fileModel.setFileName(fileEntity.getFileName());
        fileModel.setFileData(fileEntity.getFileData());
    }

}
//PRD_0133 #10181 add JFE end