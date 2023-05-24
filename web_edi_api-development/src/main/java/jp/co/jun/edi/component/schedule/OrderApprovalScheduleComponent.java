package jp.co.jun.edi.component.schedule;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.OrderReceiveCreatePdfComponent;
import jp.co.jun.edi.component.OrderReceiveCreateXmlComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.mail.MailSenderComponent;
import jp.co.jun.edi.entity.TOrderApprovalSendMailEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TOrderApprovalSendMailRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.SendMailStatusType;
import jp.co.jun.edi.type.SendMailType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 発注承認PDF（即時）メール送信コンポーネント.
 */
@Slf4j
@Component
public class OrderApprovalScheduleComponent {
    /** 種別. */
    private static final String PDF_FILENAME_TYPE_HC = "HC";

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private OrderReceiveCreateXmlComponent createXmlComponent;

    @Autowired
    private OrderReceiveCreatePdfComponent createPdfComponent;

    @Autowired
    private MailSenderComponent mailSenderComponent;

    @Autowired
    private TOrderApprovalSendMailRepository tOrderApprovalSendMailRepository;

    @Autowired
    private TOrderRepository tOrderRepository;

    /**
     *
     * 発注承認PDF（即時）メール送信実行.
     * @param userId ユーザID
     * @param tOrderApprovalSendMailEntity 発注承認メール送信情報
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void execute(final BigInteger userId, final TOrderApprovalSendMailEntity tOrderApprovalSendMailEntity) {
        log.info(LogStringUtil.of("execute")
                .message("Start processing of OrderApprovalSendMailSchedule.")
                .value("id", tOrderApprovalSendMailEntity.getId())
                .value("order_id", tOrderApprovalSendMailEntity.getOrderId())
                .value("created_only_pdf", tOrderApprovalSendMailEntity.getCreatedOnlyPdf())
                .build());

        try {
            // プロパティ情報取得
            // 一時フォルダ
            final String temporayFolder = getTemporaryPath(tOrderApprovalSendMailEntity.getId());
            // XMLファイルパス
            final Path xmlPath = generatedXmlFilePath(temporayFolder, tOrderApprovalSendMailEntity.getOrderId());
            // PDFファイルパス
            final Path pdfPath = generatedPdfFilePath(temporayFolder, tOrderApprovalSendMailEntity.getOrderId());
            // XSLファイルパス
            final Path xslPath = getXslPath();
            // PDFファイル名
            final String fileName = generatedPDFFileName(tOrderApprovalSendMailEntity.getOrderId());

            // XML作成
            createXmlComponent.createXml(tOrderApprovalSendMailEntity.getOrderId(), xmlPath);

            // PDF作成
            createPdfComponent.createPdf(userId, tOrderApprovalSendMailEntity.getOrderId(), xslPath, xmlPath, pdfPath, fileName);

            // PDFのみ作成フラグがfalse、かつ、発注承認バッチ.メール送信フラグがtrueの場合、メールを送信する
            final boolean isCreatedOnlyPdf = tOrderApprovalSendMailEntity.getCreatedOnlyPdf().getValue();
            final boolean isMailSend = propertyComponent.getBatchProperty().isOrderApprovalSendMailSend();
            if (!isCreatedOnlyPdf && isMailSend) {
                // メール送信
                sendMail(tOrderApprovalSendMailEntity);
            }

            // 一時フォルダのXMLファイル、PDFファイルを削除
            deleteFile(temporayFolder, xmlPath, pdfPath);

            log.info(LogStringUtil.of("execute")
                    .message("End processing of OrderApprovalSendMailSchedule.")
                    .value("id", tOrderApprovalSendMailEntity.getId())
                    .value("order_id", tOrderApprovalSendMailEntity.getOrderId())
                    .value("created_only_pdf", tOrderApprovalSendMailEntity.getCreatedOnlyPdf())
                    .build());

            // ステータスを 処理完了 に更新
            updateStatus(SendMailStatusType.COMPLETED, tOrderApprovalSendMailEntity.getId(), userId);
        } catch (ResourceNotFoundException e) {
            log.warn(e.getMessage(), e);
            // ステータスを 処理済み、かつ、警告あり に更新
            updateStatus(SendMailStatusType.COMPLETED_WARN, tOrderApprovalSendMailEntity.getId(), userId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // ステータスを エラー に更新
            updateStatus(SendMailStatusType.ERROR, tOrderApprovalSendMailEntity.getId(), userId);
        }

        return;
    }

    /**
     * メール送信.
     * @param tOrderApprovalSendMailEntity 送信メール情報
     * @throws Exception 例外
     */
    private void sendMail(final TOrderApprovalSendMailEntity tOrderApprovalSendMailEntity) throws Exception {
        final SendMailType sendMailType = mailSenderComponent.sendMail(
                tOrderApprovalSendMailEntity.getFromMailAddress(), tOrderApprovalSendMailEntity.getToMailAddress(),
                tOrderApprovalSendMailEntity.getCcMailAddress(), tOrderApprovalSendMailEntity.getBccMailAddress(),
                tOrderApprovalSendMailEntity.getSubject(), tOrderApprovalSendMailEntity.getMessageBody());

        if (sendMailType == SendMailType.MAIL_UNSENT || sendMailType == SendMailType.MAIL_SENDING_ERROR) {
            // メール送信されていない場合エラーとする
            throw new ScheduleException(LogStringUtil.of("sendMail")
                    .message("The email was not sent.")
                    .value("id", tOrderApprovalSendMailEntity.getId())
                    .value("order_id", tOrderApprovalSendMailEntity.getOrderId())
                    .value("created_only_pdf", tOrderApprovalSendMailEntity.getCreatedOnlyPdf())
                    .value("from_mail_address", tOrderApprovalSendMailEntity.getFromMailAddress())
                    .value("to_mail_address", tOrderApprovalSendMailEntity.getToMailAddress())
                    .value("cc_mail_address", tOrderApprovalSendMailEntity.getCcMailAddress())
                    .value("bcc_mail_address", tOrderApprovalSendMailEntity.getBccMailAddress())
                    .build());
        }
    }

    /**
     * XMLファイルパスを生成.
     *
     * @param path 一時フォルダ
     * @param orderId 発注ID
     * @return XMLファイルパス
     */
    private Path generatedXmlFilePath(final String path, final BigInteger orderId) {
        // XMLファイル order_approval_{発注ID}_{yyyyMMddHHmmssSSS}.xml
        final String xmlFile = String.format("order_approval_%s_%s.xml",
                orderId.toString(),
                DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssSSS"));
        return Paths.get(path + xmlFile);
    }

    /**
     * PDFファイルパスを生成.
     *
     * @param path 一時フォルダ
     * @param orderId 発注ID
     * @return XMLファイルパス
     */
    private Path generatedPdfFilePath(final String path, final BigInteger orderId) {
        // XMLファイル order_approval_{発注ID}_{yyyyMMddHHmmssSSS}.pdf
        final String pdfFile = String.format("order_approval_%s_%s.pdf",
                orderId.toString(),
                DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssSSS"));

        return Paths.get(path + pdfFile);
    }

    /**
     * 一時ディレクトリを取得する.
     * ディレクトリ構成：{一時ディレクトリ}/orderApproval/{メール送信ID}/
     * @param sendMailId メール送信ID
     * @return 発注別の一時ディレクトリ
     * @throws Exception 例外
     */
    private String getTemporaryPath(final BigInteger sendMailId) throws Exception {
        String tmpDirectory = propertyComponent.getBatchProperty().getScheduleFopTemporaryFolder();
        if (!tmpDirectory.endsWith("/")) {
            // 接尾辞がスラッシュでない場合、接尾辞にスラッシュをつける
            tmpDirectory = tmpDirectory.concat("/");
        }
        tmpDirectory = tmpDirectory.concat("orderApproval/").concat(sendMailId.toString()).concat("/");
        Files.createDirectories(Paths.get(tmpDirectory));

        return tmpDirectory;
    }

    /**
     * XSLファイルパスを取得.
     * @return XSLファイルパス
     * @throws Exception 例外
     */
    private Path getXslPath() throws Exception {
        final String strXslPath = propertyComponent.getBatchProperty().getOrderApprovalPathXsl();
        final Path xslPath = Paths.get(strXslPath);
        if (!Files.exists(xslPath)) {
            throw new ScheduleException(LogStringUtil.of("getXslPath")
                    .message("file not exist.")
                    .value("order-approval.path.xsl", strXslPath)
                    .build());
        }
        return xslPath;
    }

    /**
     * 処理対象の発注承認メール送信情報のステータスを更新.
     * @param status OrderSendMailStatusType
     * @param orderSendMailId 受信確定メール送信情報ID
     * @param userId ユーザID
     */
    private void updateStatus(final SendMailStatusType status, final BigInteger orderSendMailId, final BigInteger userId) {
        tOrderApprovalSendMailRepository.updateStatusById(status.getValue(), orderSendMailId, userId);
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
     * 発注承認PDFファイル名 生成.
     *
     * 品番CHAR(8)-種別CHAR(2)発注番号CHAR(8)SH.pdf
     * 例）ANM59050-HC100012SH.pdf
     *
     * @param orderId 発注ID
     * @return PDF名
     */
    private String generatedPDFFileName(final BigInteger orderId) {
        final TOrderEntity order = tOrderRepository.findByOrderId(orderId).orElseThrow(
                () -> new ResourceNotFoundException(
                        ResultMessages.warning().add(
                                MessageCodeType.CODE_002, LogStringUtil.of("generatedPDFFileName")
                                        .message("t_order not found.")
                                        .value("order_id", orderId)
                                        .build())));

        // ファイル名：品番CHAR(8)-種別CHAR(2)発注番号CHAR(8).pdf
        final String fileName = String.format("%s-%s%sSH.pdf", order.getPartNo(), PDF_FILENAME_TYPE_HC, order.getOrderNumber());

        return fileName;
    }

}
