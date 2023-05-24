package jp.co.jun.edi.component.schedule;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.DeliveryRequestCreatePdfComponent;
import jp.co.jun.edi.component.DeliveryRequestCreateXmlComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.mail.MailAddressComponent;
import jp.co.jun.edi.component.mail.MailSenderAttachmentComponent;
import jp.co.jun.edi.component.model.MailAttachementFileModel;
import jp.co.jun.edi.component.model.TemporaryFileForPdfGenerationModel;
import jp.co.jun.edi.entity.TDeliveryOfficialSendMailEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.repository.TDeliveryOfficialSendMailRepository;
import jp.co.jun.edi.type.SendMailStatusType;
import jp.co.jun.edi.type.SendMailType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 納品依頼正式メール送信コンポーネント.
 */
@Slf4j
@Component
public class DeliveryRequestOfficialScheduleComponent {
    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private DeliveryRequestCreateXmlComponent createXmlComponent;

    @Autowired
    private DeliveryRequestCreatePdfComponent createPdfComponent;

    @Autowired
    private MailSenderAttachmentComponent mailSenderAttachmentComponent;

    @Autowired
    private TDeliveryOfficialSendMailRepository tDeliveryOfficialSendMailRepository;

    @Autowired
    private MailAddressComponent mailAddressComponent;

    /**
     *
     * 納品依頼正式メール送信実行.
     * @param listTDeliveryOfficialSendMailEntity 納品依頼正式メール送信情報リスト
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void execute(final List<TDeliveryOfficialSendMailEntity> listTDeliveryOfficialSendMailEntity, final BigInteger userId) {

        final TDeliveryOfficialSendMailEntity firstEntity = listTDeliveryOfficialSendMailEntity.get(0);
        log.info(LogStringUtil.of("execute")
                .message("Start processing of DeliveryRequestOfficialSendMailSchedule.")
                .value("mdf_maker_code", firstEntity.getMdfMakerCode())
                .value("mdf_maker_factory_code", firstEntity.getMdfMakerFactoryCode())
                .value("delivery_id", firstEntity.getDeliveryId())
                .build());

        try {

            // PDFを作成する
            final List<TemporaryFileForPdfGenerationModel> files = createPdfs(listTDeliveryOfficialSendMailEntity, userId);
            if (files.isEmpty()) {
                // 添付ファイル0件の場合、警告ログを出力しメールは送信しない
                log.warn(LogStringUtil.of("execute")
                        .message("There are no attachments.")
                        .value("mdf_maker_code", firstEntity.getMdfMakerCode())
                        .value("mdf_maker_factory_code", firstEntity.getMdfMakerFactoryCode())
                        .value("delivery_id", firstEntity.getDeliveryId())
                        .build());
                return;
            }

            // PDFのみ作成フラグがfalse、かつ、納品依頼正式バッチ.メール送信フラグがtrueの場合、メールを送信する
            final boolean isCreatedOnlyPdf = firstEntity.getCreatedOnlyPdf().getValue();
            final boolean isMailSend = propertyComponent.getBatchProperty().isDeliveryOfficialRequestSendMailSend();
            if (!isCreatedOnlyPdf && isMailSend) {
                // メール送信
                sendMail(firstEntity, files);
            }

            // 一時フォルダのXMLファイル、PDFファイルを削除
            deleteFile(files);

            // ステータスを 処理完了 に更新
            updateStatusBySendMailList(SendMailStatusType.COMPLETED, listTDeliveryOfficialSendMailEntity, userId);

            log.info(LogStringUtil.of("execute")
                    .message("End processing of DeliveryRequestOfficialSendMailSchedule.")
                    .value("mdf_maker_code", firstEntity.getMdfMakerCode())
                    .value("mdf_maker_factory_code", firstEntity.getMdfMakerFactoryCode())
                    .value("delivery_id", firstEntity.getDeliveryId())
                    .build());
        } catch (ResourceNotFoundException e) {
            log.warn(e.getMessage(), e);
            // ステータスを 処理済み、かつ、警告あり に更新
            updateStatusBySendMailList(SendMailStatusType.COMPLETED_WARN, listTDeliveryOfficialSendMailEntity, userId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // ステータスを エラー に更新
            updateStatusBySendMailList(SendMailStatusType.ERROR, listTDeliveryOfficialSendMailEntity, userId);
        }

        return;
    }

    /**
     * メール送信.
     * @param sendMailInfo 送信メール情報
     * @param files 添付ファイル情報
     */
    private void sendMail(final TDeliveryOfficialSendMailEntity sendMailInfo, final List<TemporaryFileForPdfGenerationModel> files) {
        // TODO (内部課題No113対応完了までの仮仕様）
        // 「m_kojmst.kojcd」と「t_item.mdf_maker_factory_code」を突合する場合は、
        // 「t_item.mdf_maker_factory_code」がNULLの場合、空文字に変換する

        // 夜間バッチまでの間に、工場マスタのメールアドレスが変更された場合、
        // 古いメールアドレスに送信してしまう不整合をふせぐため、BCCメールアドレスを再取得
        final String bccMaileAddress = mailAddressComponent.getDeliveryMdfMakerFactoryMailaddress(sendMailInfo.getDeliveryId(), sendMailInfo.getMdfMakerCode(),
                Optional.ofNullable(sendMailInfo.getMdfMakerFactoryCode()).orElse(StringUtils.EMPTY));
        if (StringUtils.equals(sendMailInfo.getBccMailAddress(), bccMaileAddress)) {
            // メールアドレスが変更されていることを、ログに出力する
            log.info(LogStringUtil.of("sendMail")
                    .message("Email address has changed.")
                    .value("id", sendMailInfo.getId())
                    .value("delivery_id", sendMailInfo.getDeliveryId())
                    .value("mdf_maker_code", sendMailInfo.getMdfMakerCode())
                    .value("mdf_maker_factory_code", sendMailInfo.getMdfMakerFactoryCode())
                    .value("before bcc_mail_address", sendMailInfo.getBccMailAddress())
                    .value("after bcc_mail_address", bccMaileAddress)
                    .build());
        }

        // PDFファイルのリストを抽出
        final List<MailAttachementFileModel> listFile = files.stream().map(file -> {
            final MailAttachementFileModel mailAttachementFileModel = new MailAttachementFileModel();
            mailAttachementFileModel.setFile(file.getPdfFilePath().toFile());
            mailAttachementFileModel.setFileName(file.getFileName());
            return mailAttachementFileModel;
        }).collect(Collectors.toList());

        // メール送信
        final SendMailType sendMailType = mailSenderAttachmentComponent.sendMail(
                sendMailInfo.getFromMailAddress(),
                sendMailInfo.getToMailAddress(),
                sendMailInfo.getCcMailAddress(),
                bccMaileAddress,
                sendMailInfo.getSubject(),
                sendMailInfo.getMessageBody(),
                listFile);

        if (sendMailType == SendMailType.MAIL_UNSENT || sendMailType == SendMailType.MAIL_SENDING_ERROR) {
            // メール送信されていない場合エラーとする
            throw new ScheduleException(LogStringUtil.of("sendMail")
                    .message("The email was not sent.")
                    .value("id", sendMailInfo.getId())
                    .value("delivery_id", sendMailInfo.getDeliveryId())
                    .value("created_only_pdf", sendMailInfo.getCreatedOnlyPdf())
                    .value("from_mail_address", sendMailInfo.getFromMailAddress())
                    .value("to_mail_address", sendMailInfo.getToMailAddress())
                    .value("cc_mail_address", sendMailInfo.getCcMailAddress())
                    .value("bcc_mail_address", bccMaileAddress)
                    .build());
        }
    }

    /**
     * PDFを生成する.
     * @param listTDeliveryOfficialSendMailEntity 納品依頼正式メール送信情報リスト
     * @param userId ユーザID
     * @return 一時フォルダに生成されたファイル情報
     */
    private List<TemporaryFileForPdfGenerationModel> createPdfs(final List<TDeliveryOfficialSendMailEntity> listTDeliveryOfficialSendMailEntity,
            final BigInteger userId) {
        // 一時フォルダに生成されたXMLファイル、PDFファイル格納用
        final List<TemporaryFileForPdfGenerationModel> files = new ArrayList<>();

        listTDeliveryOfficialSendMailEntity.stream().forEach(tDeliveryOfficialSendMailEntity -> {
            try {
                log.info(LogStringUtil.of("createPdfs")
                        .message("Start processing of DeliveryRequestOfficialSendMailSchedule.")
                        .value("id", tDeliveryOfficialSendMailEntity.getId())
                        .value("delivery_id", tDeliveryOfficialSendMailEntity.getDeliveryId())
                        .value("created_only_pdf", tDeliveryOfficialSendMailEntity.getCreatedOnlyPdf())
                        .build());

                final TemporaryFileForPdfGenerationModel file = new TemporaryFileForPdfGenerationModel();
                // XSLファイルパス
                final Path xslPath = getXslPath();
                // 一時フォルダ
                final String temporayFolder = getTemporaryPath(tDeliveryOfficialSendMailEntity.getId());
                file.setTemporayFolder(temporayFolder);
                // XMLファイルパス
                file.setXmlFilePath(generatedXmlFilePath(temporayFolder, tDeliveryOfficialSendMailEntity));
                // PDFファイルパス
                file.setPdfFilePath(generatedPdfFilePath(temporayFolder, tDeliveryOfficialSendMailEntity));

                // XML作成
                createXmlComponent.createXml(tDeliveryOfficialSendMailEntity.getDeliveryId(), file.getXmlFilePath());

                // PDF作成
                final String pdfFileName = createPdfComponent.createPdf(userId,
                        tDeliveryOfficialSendMailEntity.getDeliveryId(),
                        xslPath,
                        file.getXmlFilePath(),
                        file.getPdfFilePath());
                // PDFファイル名
                file.setFileName(pdfFileName);

                log.info(LogStringUtil.of("createPdfs")
                        .message("End processing of DeliveryRequestOfficialSendMailSchedule.")
                        .value("id", tDeliveryOfficialSendMailEntity.getId())
                        .value("delivery_id", tDeliveryOfficialSendMailEntity.getDeliveryId())
                        .value("created_only_pdf", tDeliveryOfficialSendMailEntity.getCreatedOnlyPdf())
                        .build());

                // PDF作成に成功したものをリストに格納する
                files.add(file);
            } catch (ResourceNotFoundException e) {
                log.warn(e.getMessage(), e);
                // ステータスを 処理済み、かつ、警告あり に更新
                updateStatusBySendMailId(SendMailStatusType.COMPLETED_WARN, tDeliveryOfficialSendMailEntity.getId(), userId);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                // ステータスを エラー に更新
                updateStatusBySendMailId(SendMailStatusType.ERROR, tDeliveryOfficialSendMailEntity.getId(), userId);
            }

        });
        return files;
    }

    /**
     * ファイル削除.
     * @param fileList 削除対象ファイルリスト
     */
    private void deleteFile(final List<TemporaryFileForPdfGenerationModel> fileList) {
        fileList.stream().forEach(file -> {
            try {
                // 一時フォルダのPDFファイルを削除
                Files.delete(file.getPdfFilePath());
                // 一時フォルダのXMLファイルを削除
                Files.delete(file.getXmlFilePath());
                // 一時ファイルと発注一時ディレクトリを削除
                Files.delete(Paths.get(file.getTemporayFolder()));
            } catch (Exception e) {
                // ファイル削除失敗の場合は、ワーニングログを表示するが、処理は正常処理とする
                log.warn(e.getMessage());
            }
        });
    }

    /**
     * XMLファイルパスを生成.
     *
     * @param path 一時フォルダ
     * @param tDeliveryOfficialSendMailEntity 納品依頼正式メール送信情報
     * @return XMLファイルパス
     */
    private Path generatedXmlFilePath(final String path, final TDeliveryOfficialSendMailEntity tDeliveryOfficialSendMailEntity) {
        // XMLファイル delivery_official_{生産メーカー}_{生産工場}_{納品ID}_{yyyyMMddHHmmssSSS}.xml
        final String xmlFile = String.format("delivery_official_%s_%s_%d_%s.xml",
                Optional.ofNullable(tDeliveryOfficialSendMailEntity.getMdfMakerCode()).orElse(StringUtils.EMPTY),
                Optional.ofNullable(tDeliveryOfficialSendMailEntity.getMdfMakerFactoryCode()).orElse(StringUtils.EMPTY),
                tDeliveryOfficialSendMailEntity.getDeliveryId(),
                DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssSSS"));
        return Paths.get(path + xmlFile);
    }

    /**
     * PDFファイルパスを生成.
     *
     * @param path 一時フォルダ
     * @param tDeliveryOfficialSendMailEntity 納品依頼正式メール送信情報
     * @return PDFファイルパス
     */
    private Path generatedPdfFilePath(final String path, final TDeliveryOfficialSendMailEntity tDeliveryOfficialSendMailEntity) {
        // PDFファイル delivery_official_{生産メーカー}_{生産工場}_{納品ID}_{yyyyMMddHHmmssSSS}.pdf
        final String xmlFile = String.format("delivery_official_%s_%s_%d_%s.pdf",
                Optional.ofNullable(tDeliveryOfficialSendMailEntity.getMdfMakerCode()).orElse(StringUtils.EMPTY),
                Optional.ofNullable(tDeliveryOfficialSendMailEntity.getMdfMakerFactoryCode()).orElse(StringUtils.EMPTY),
                tDeliveryOfficialSendMailEntity.getDeliveryId(),
                DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssSSS"));
        return Paths.get(path + xmlFile);
    }

    /**
     * 一時ディレクトリを取得する.
     * ディレクトリ構成：{一時ディレクトリ}/deliveryOfficial/{メール送信ID}/
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
        tmpDirectory = tmpDirectory.concat("deliveryOfficial/").concat(sendMailId.toString()).concat("/");
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
     * 処理対象の納品依頼正式メール送信情報のステータスを更新.
     * @param status DeliverySendMailStatusType
     * @param listTDeliveryOfficialSendMailEntity 受信確定メール送信情報リスト
     * @param userId ユーザID
     */
    private void updateStatusBySendMailList(final SendMailStatusType status, final List<TDeliveryOfficialSendMailEntity> listTDeliveryOfficialSendMailEntity,
            final BigInteger userId) {
        if (listTDeliveryOfficialSendMailEntity.isEmpty()) {
            return;
        }
        final List<BigInteger> ids = listTDeliveryOfficialSendMailEntity.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tDeliveryOfficialSendMailRepository.updateStatusByIdsStatusOne(status.getValue(), ids, userId);
    }

    /**
     * 処理対象の納品依頼正式メール送信情報のステータスを更新.
     * @param status DeliverySendMailStatusType
     * @param deliverySendMailId 納品依頼正式メール送信情報ID
     * @param userId ユーザID
     */
    private void updateStatusBySendMailId(final SendMailStatusType status, final BigInteger deliverySendMailId, final BigInteger userId) {
        tDeliveryOfficialSendMailRepository.updateStatusById(status.getValue(), deliverySendMailId, userId);
    }

}
