package jp.co.jun.edi.component;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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

import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

//PRD_0134 #10654 add JEF start
/**
 * PDFファイルを生成する.
 */
@Slf4j
@Component
public class PurchaseDigestionItemCreatePdfComponent {

    @Autowired
    private PropertyComponent propertyComponent;

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
    public void createPdf(final Path xslPath, final Path xmlPath, final Path pdfPath,
             final String fileName)
            throws Exception {
        log.info(LogStringUtil.of("createPdf")
                .message("Start processing of createPDF.")
                .value("Filename", fileName)
                .build());

        // PDFファイル生成
        genaratedPdf(xslPath, xmlPath, pdfPath);
        log.info(LogStringUtil.of("createPdf")
                .message("End processing of createPDF.")
                .value("Filename", fileName)
                .build());
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

}
//PRD_0134 #10654 add JEF end