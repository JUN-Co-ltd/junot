package jp.co.jun.edi.component.schedule;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.DeliveryRequestCreatePdfComponent;
import jp.co.jun.edi.component.DeliveryRequestCreateXmlComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.mail.MailSenderComponent;
import jp.co.jun.edi.entity.TDeliverySendMailEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.repository.TDeliverySendMailRepository;
import jp.co.jun.edi.type.SendMailStatusType;
import jp.co.jun.edi.type.SendMailType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 納品承認（即時）メール送信コンポーネント.
 */
@Slf4j
@Component
public class DeliveryRequestScheduleComponent {

    private static final String SEPARATED_STR = ",";
    @Autowired
    private MailSenderComponent mailSenderComponent;

    @Autowired
    private TDeliverySendMailRepository tDeliverySendMailRepository;

    @Autowired
    private DeliveryRequestCreateXmlComponent deliveryRequestCreateXmlComponent;

    @Autowired
    private DeliveryRequestCreatePdfComponent deliveryRequestCreatePdfComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    /**
     *
     * 納品承認（即時）メール送信実行.
     * @param tDeliverySendMailEntity 納品承認（即時）メール送信情報
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void execute(final TDeliverySendMailEntity tDeliverySendMailEntity, final BigInteger userId) {
        log.info(LogStringUtil.of("execute")
                .message("Start processing of DeliveryRequestRealTimeMailSchedule.")
                .value("id", tDeliverySendMailEntity.getId())
                .value("delivery_id", tDeliverySendMailEntity.getDeliveryId())
                .value("created_only_pdf", tDeliverySendMailEntity.getCreatedOnlyPdf())
                .build());

        try {
            // プロパティ情報取得
            // XSLファイルパス
            final Path xslPath = getXslPath();
            // 一時フォルダ
            final String temporayFolder = getTemporaryPath(tDeliverySendMailEntity.getId());
            // 一時フォルダに格納するXMLファイルパス
            final Path xmlPath = generatedXmlFilePath(temporayFolder, tDeliverySendMailEntity.getDeliveryId());
            // 一時フォルダに格納するPDFファイルパス
            final Path pdfPath = generatedPdfFilePath(temporayFolder, tDeliverySendMailEntity.getDeliveryId());

            // XML作成
            deliveryRequestCreateXmlComponent.createXml(tDeliverySendMailEntity.getDeliveryId(), xmlPath);
            // PDF作成
            deliveryRequestCreatePdfComponent.createPdf(userId, tDeliverySendMailEntity.getDeliveryId(), xslPath, xmlPath, pdfPath);

            // PDFのみ作成フラグがfalse、かつ、受注確定バッチ.メール送信フラグがtrueの場合、メールを送信する
            final boolean isCreatedOnlyPdf = tDeliverySendMailEntity.getCreatedOnlyPdf().getValue();
            final boolean isMailSend = propertyComponent.getBatchProperty().isDeliveryRequestSendMailSend();
            if (!isCreatedOnlyPdf && isMailSend) {
                //メール送信
                sendMail(tDeliverySendMailEntity);
            }

            // 一時フォルダのXMLファイル、PDFファイルを削除
            deleteFile(temporayFolder, xmlPath, pdfPath);

            // 処理済み更新
            updateStatus(userId, tDeliverySendMailEntity.getId(), SendMailStatusType.COMPLETED);

            log.info(LogStringUtil.of("execute")
                    .message("End processing of DeliveryRequestRealTimeMailSchedule.")
                    .value("id", tDeliverySendMailEntity.getId())
                    .value("delivery_id", tDeliverySendMailEntity.getDeliveryId())
                    .value("created_only_pdf", tDeliverySendMailEntity.getCreatedOnlyPdf())
                    .build());
        } catch (ResourceNotFoundException e) {
            log.warn(e.getMessage(), e);
            // ステータスを 処理済み、かつ、警告あり に更新
            updateStatus(userId, tDeliverySendMailEntity.getId(), SendMailStatusType.COMPLETED_WARN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // ステータスを エラー に更新
            updateStatus(userId, tDeliverySendMailEntity.getId(), SendMailStatusType.ERROR);
        }
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
     * XMLファイルパスを生成.
     *
     * @param path 一時フォルダ
     * @param deliveryId 納品ID
     * @return XMLファイルパス
     */
    private Path generatedXmlFilePath(final String path, final BigInteger deliveryId) {
        // XMLファイル delivery_{納品ID}_{yyyyMMddHHmmssSSS}.xml
        final String xmlFile = "delivery_" + deliveryId.toString() + DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssSSS") + ".xml";
        return Paths.get(path + xmlFile);
    }

    /**
     * PDFファイルパスを生成.
     *
     * @param path 一時フォルダ
     * @param deliveryId 納品ID
     * @return XMLファイルパス
     */
    private Path generatedPdfFilePath(final String path, final BigInteger deliveryId) {
        // XMLファイル delivery_{納品ID}_{yyyyMMddHHmmssSSS}.xml
        final String pdfFile = "delivery_" + deliveryId.toString() + DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssSSS") + ".pdf";

        return Paths.get(path + pdfFile);
    }

    /**
     * 一時ディレクトリを取得する.
     * ディレクトリ構成：{一時ディレクトリ}/delivery/{メール送信ID}/
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
        tmpDirectory = tmpDirectory.concat("delivery/").concat(sendMailId.toString()).concat("/");
        Files.createDirectories(Paths.get(tmpDirectory));

        return tmpDirectory;
    }

    /**
     * XSLファイルパスを取得.
     * @return XSLファイルパス
     * @throws Exception 例外
     */
    private Path getXslPath() throws Exception {
        final String strXslPath = propertyComponent.getBatchProperty().getDeliveryRequestPathXsl();
        final Path xslPath = Paths.get(strXslPath);
        if (!Files.exists(xslPath)) {
            throw new ScheduleException(LogStringUtil.of("getXslPath")
                    .message("file not exist.")
                    .value("delivery-request.path.xsl", strXslPath)
                    .build());
        }
        return xslPath;
    }

    /**
     * 納品依頼メール送信管理を更新する.
     * @param userId ユーザID
     * @param sendMailId メール送信ID
     * @param type メールの送信状態分類
     * @return 更新件数
     */
    public int updateStatus(final BigInteger userId, final BigInteger sendMailId, final SendMailStatusType type) {
        return tDeliverySendMailRepository.updateStatus(type.getValue(), userId, sendMailId);
    }

    /**
     * CSVをメール送信する.
     * @param tDeliverySendMailEntity 納品依頼メール送信管理
     */
    public void sendMail(final TDeliverySendMailEntity tDeliverySendMailEntity) {

        // メール情報の格納
        final String[] toMailAddress = StringUtils.split(tDeliverySendMailEntity.getToMailAddress(), SEPARATED_STR);
        final String[] ccMailAddress = StringUtils.split(tDeliverySendMailEntity.getCcMailAddress(), SEPARATED_STR);
        final String[] bccMailAddress = StringUtils.split(tDeliverySendMailEntity.getBccMailAddress(), SEPARATED_STR);
        final String fromMailAddress = tDeliverySendMailEntity.getFromMailAddress();
        final String subject = tDeliverySendMailEntity.getSubject();
        final String messageBody = tDeliverySendMailEntity.getMessageBody();

        // メール送信
        final SendMailType sendMailType = mailSenderComponent.sendMail(fromMailAddress, toMailAddress, ccMailAddress, bccMailAddress, subject, messageBody);

        if (sendMailType == SendMailType.MAIL_UNSENT || sendMailType == SendMailType.MAIL_SENDING_ERROR) {
            // メール送信されていない場合エラーとする
            throw new ScheduleException(LogStringUtil.of("sendMail")
                    .message("The email was not sent.")
                    .value("id", tDeliverySendMailEntity.getId())
                    .value("delivery_id", tDeliverySendMailEntity.getDeliveryId())
                    .value("created_only_pdf", tDeliverySendMailEntity.getCreatedOnlyPdf())
                    .value("from_mail_address", tDeliverySendMailEntity.getFromMailAddress())
                    .value("to_mail_address", tDeliverySendMailEntity.getToMailAddress())
                    .value("cc_mail_address", tDeliverySendMailEntity.getCcMailAddress())
                    .value("bcc_mail_address", tDeliverySendMailEntity.getBccMailAddress())
                    .build());

        }

    }

}
