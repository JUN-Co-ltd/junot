package jp.co.jun.edi.component.schedule;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.ReturnItemCreatePdfComponent;
import jp.co.jun.edi.component.ReturnItemCreateXmlComponent;
import jp.co.jun.edi.entity.TMakerReturnEntity;
import jp.co.jun.edi.entity.TReturnVoucherEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TMakerReturnRepository;
import jp.co.jun.edi.repository.TReturnVoucherRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.SendMailStatusType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 返品明細メール送信コンポーネント.
 */
@Slf4j
@Component
public class ReturnItemScheduleComponent {
    /** PDFファイル.ファイル名.種別(JR：返品明細). */
    private static final String PDF_FILENAME_TYPE_JR = "JR";

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private TReturnVoucherRepository tReturnVoucherRepository;

    @Autowired
    private TMakerReturnRepository tMakerReturnRepository;

    @Autowired
    private ReturnItemCreateXmlComponent createXmlComponent;

    @Autowired
    private ReturnItemCreatePdfComponent createPdfComponent;

    /**
     *
     * 返品伝票作成処理の実行.
     * @param tReturnVoucherEntity 返品伝票管理情報
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void execute(final TReturnVoucherEntity tReturnVoucherEntity) {
        log.info(LogStringUtil.of("execute")
                .message("Start processing of ReturnItemSchedule.")
                .value("id", tReturnVoucherEntity.getId())
                .value("voucher_number", tReturnVoucherEntity.getVoucherNumber())
                .value("order_id", tReturnVoucherEntity.getOrderId())
                .build());

        final BigInteger userId = scheduleBusinessComponent.getUserId();

        try {
            // プロパティ情報取得
            // 一時フォルダ
            final String temporayFolder = getTemporaryPath(tReturnVoucherEntity.getId());
            // XMLファイルパス
            final Path xmlPath = generatedXmlFilePath(temporayFolder, tReturnVoucherEntity.getVoucherNumber(), tReturnVoucherEntity.getOrderId());
            // PDFファイルパス
            final Path pdfPath = generatedPdfFilePath(temporayFolder, tReturnVoucherEntity.getVoucherNumber(), tReturnVoucherEntity.getOrderId());
            // XSLファイルパス
            final Path xslPath = getXslPath();
            // PDFファイル名
            final String fileName = generatedPDFFileName(tReturnVoucherEntity.getVoucherNumber(), tReturnVoucherEntity.getOrderId());

            // XML作成
            // PRD_0073 mod SIT start
            //createXmlComponent.createXml(tReturnVoucherEntity.getVoucherNumber(), tReturnVoucherEntity.getOrderId(), xmlPath);
            createXmlComponent.createXml(tReturnVoucherEntity.getVoucherNumber(), tReturnVoucherEntity.getOrderId(),
                                            tReturnVoucherEntity.getCreatedAt(), xmlPath);
            // PRD_0073 mod SIT start

            // PDF作成
            final String voucherNumber = tReturnVoucherEntity.getVoucherNumber();
            final BigInteger ordrId = tReturnVoucherEntity.getOrderId();
            createPdfComponent.createPdf(userId, voucherNumber, ordrId, xslPath, xmlPath, pdfPath, fileName);

            // 一時フォルダのXMLファイル、PDFファイルを削除
            deleteFile(temporayFolder, xmlPath, pdfPath);

            log.info(LogStringUtil.of("execute")
                    .message("End processing of ReturnItemSchedule.")
                    .value("id", tReturnVoucherEntity.getId())
                    .value("voucher_number", tReturnVoucherEntity.getVoucherNumber())
                    .value("order_id", tReturnVoucherEntity.getOrderId())
                    .build());

            // ステータスを 処理完了 に更新
            updateStatus(SendMailStatusType.COMPLETED, tReturnVoucherEntity.getId(), userId);
        } catch (ResourceNotFoundException e) {
            log.warn(e.getMessage(), e);
            // ステータスを 処理済み、かつ、警告あり に更新
            updateStatus(SendMailStatusType.COMPLETED_WARN, tReturnVoucherEntity.getId(), userId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // ステータスを エラー に更新
            updateStatus(SendMailStatusType.ERROR, tReturnVoucherEntity.getId(), userId);
        }
    }

    /**
     * 一時ディレクトリを取得する.
     * ディレクトリ構成：{一時ディレクトリ}/returnItem/{ユーザID}/
     * @param userId ユーザID
     * @return 発注別の一時ディレクトリ
     * @throws Exception 例外
     */
    private String getTemporaryPath(final BigInteger userId) throws Exception {
        String tmpDirectory = propertyComponent.getBatchProperty().getScheduleFopTemporaryFolder();
        if (!tmpDirectory.endsWith("/")) {
            // 接尾辞がスラッシュでない場合、接尾辞にスラッシュをつける
            tmpDirectory = tmpDirectory.concat("/");
        }
        tmpDirectory = tmpDirectory.concat("returnItem/").concat(userId.toString()).concat("/");
        Files.createDirectories(Paths.get(tmpDirectory));

        return tmpDirectory;
    }

    /**
     * XMLファイルパスを生成.
     *
     * @param path 一時フォルダ
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @return XMLファイルパス
     */
    private Path generatedXmlFilePath(final String path, final String voucherNumber, final BigInteger orderId) {
        // XMLファイル return_item_{伝票番号}_{発注ID}_{yyyyMMddHHmmssSSS}.xml
        final String nowDate = DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssS");
        final String xmlFile = "return_item_" + voucherNumber + "_" + orderId.toString() + "_" + nowDate + ".xml";
        return Paths.get(path + xmlFile);
    }

    /**
     * PDFファイルパスを生成.
     *
     * @param path 一時フォルダ
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @return XMLファイルパス
     */
    private Path generatedPdfFilePath(final String path, final String voucherNumber, final BigInteger orderId) {
        // PDFファイル return_item_{伝票番号}_{発注ID}_{yyyyMMddHHmmssSSS}.xml
        final String nowDate = DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssS");
        final String pdfFile = "return_item_" + voucherNumber + "_" + orderId.toString() + "_" + nowDate + ".pdf";

        return Paths.get(path + pdfFile);
    }

    /**
     * XSLファイルパスを取得.
     * @return XSLファイルパス
     * @throws Exception 例外
     */
    private Path getXslPath() throws Exception {
        final String strXslPath = propertyComponent.getBatchProperty().getReturnItemPathXsl();
        final Path xslPath = Paths.get(strXslPath);
        if (!Files.exists(xslPath)) {
            throw new ScheduleException(LogStringUtil.of("getXslPath")
                    .message("not find path.")
                    .value("return-item.path.xsl", strXslPath)
                    .build());
        }
        return xslPath;
    }

    /**
     * 返品明細PDFファイル名 生成.
     *
     * 品番CHAR(8)-種別CHAR(2)発注番号CHAR(8)種別CHAR(2)
     * 例）ANM59050-JK100012.pdf
     *
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @return PDF名
     */
    private String generatedPDFFileName(final String voucherNumber, final BigInteger orderId) {
        final TMakerReturnEntity makerReturn = tMakerReturnRepository.findByVoucherNumberAndOrderIdAndVoucherLine1(voucherNumber, orderId).orElseThrow(
                () -> new ResourceNotFoundException(
                        ResultMessages.warning().add(
                                MessageCodeType.CODE_002, LogStringUtil.of("generatedPDFFileName")
                                        .message("t_maker_return not found.")
                                        .value("voucher_number", voucherNumber)
                                        .build())));

        // ファイル名：品番CHAR(8)-種別CHAR(2)発注番号CHAR(8)種別CHAR(2).pdf
        final String partNo = makerReturn.getPartNo();
        final BigInteger orderNumber = makerReturn.getOrderNumber();
        final String fileName = String.format("%s-%s%s%s.pdf", partNo, PDF_FILENAME_TYPE_JR, orderNumber, PDF_FILENAME_TYPE_JR);

        return fileName;
    }

    /**
     * ファイル削除.
     * @param temporayFolder 一時フォルダ
     * @param xmlPath XMLファイルパス
     * @param pdfPath PDFファイルパス
     */
    private void deleteFile(final String temporayFolder, final Path xmlPath, final Path pdfPath) {
        try {
            // 一時フォルダのXMLファイルを削除
            Files.delete(xmlPath);
            // 一時フォルダのPDFファイルを削除
            Files.delete(pdfPath);
            // 一時ファイルと発注一時ディレクトリを削除
            Files.delete(Paths.get(temporayFolder));
        } catch (Exception e) {
            // ファイル削除失敗の場合は、ワーニングログを表示するが、処理は正常処理とする
            log.warn(e.getMessage());
        }
    }

    /**
     * 処理対象の返品伝票管理情報のステータスを更新.
     * @param status SendMailStatusType
     * @param voucherNumber 返品伝票管理情報ID
     * @param userId ユーザID
     */
    private void updateStatus(final SendMailStatusType status, final BigInteger voucherNumber, final BigInteger userId) {
        tReturnVoucherRepository.updateStatusById(status.getValue(), voucherNumber, userId);
    }
}
