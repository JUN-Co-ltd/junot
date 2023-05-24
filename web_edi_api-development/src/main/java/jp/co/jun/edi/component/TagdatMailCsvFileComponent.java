package jp.co.jun.edi.component;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.mail.MailSenderAttachmentComponent;
import jp.co.jun.edi.component.mail.VelocityMailTemplateComponent;
import jp.co.jun.edi.component.model.CommonPropertyModel;
import jp.co.jun.edi.component.model.MailAttachementFileModel;
import jp.co.jun.edi.component.model.VelocityConvertedMailTemplateModel;
import jp.co.jun.edi.entity.AdrmstEntity;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.model.TagdatMailInfoBrandCountModel;
import jp.co.jun.edi.model.TagdatMailInfoModel;
import jp.co.jun.edi.repository.TagdatRepository;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.type.SendMailType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * TAGDAT CSVファイルメール送信コンポーネント.
 */
@Component
@Slf4j
public class TagdatMailCsvFileComponent {
    @Autowired
    private MailSenderAttachmentComponent mailSenderAttachmentComponent;

    @Autowired
    private VelocityMailTemplateComponent<TagdatMailInfoModel> velocityMailTemplateComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TagdatRepository tagdatRepository;

    /** CSVファイル：接頭辞. */
    private static final String TMP_FILE_PREFIX_INFO = "Tagdat";

    /** CSVファイル：接尾辞. */
    private static final String TMP_FILE_SUFFIX_INFO = ".csv";

    /** TAGDAT送信ステータス：送信エラー. */
    private static final int ERROR = 2;

    /**
     * CSVをメール送信する.
     * @param listAdrmstEntity アドレスマスタ情報
     * @param userId ユーザID
     */
    public void mailCsvFile(final List<AdrmstEntity> listAdrmstEntity, final BigInteger userId) {

    	final AdrmstEntity firstEntity = listAdrmstEntity.get(0);

    	// ブランドリスト取得
    	final List<String> brands = new ArrayList<String>();
        listAdrmstEntity.stream().forEach(data -> {
        	brands.add(data.getBrand01_60());
        });

        // メール本文情報取得
        final TagdatMailInfoModel sendModel = new TagdatMailInfoModel();
        final CommonPropertyModel commonPropertyModel = propertyComponent.getCommonProperty();
        //タイトル.接頭語
        sendModel.setSubjectPrefix(commonPropertyModel.getSendMailTemplateEmbeddedCharacterSubjectPrefix());
        // 署名
        sendModel.setSignature(commonPropertyModel.getSendMailSignature());

        final List<TagdatMailInfoBrandCountModel> messages = brands.stream().map(brand -> {
        	final TagdatMailInfoBrandCountModel model = new TagdatMailInfoBrandCountModel();
        	final int count = tagdatRepository.countByBrand(brand);
        	model.setBrand(brand);
        	model.setCount(count);
        	return model;
        }).collect(Collectors.toList());
        // 本文：メッセージ
        sendModel.setMessages(messages);

        // テンプレート生成
        final VelocityConvertedMailTemplateModel optionalMailModel = velocityMailTemplateComponent.convert(sendModel, MMailCodeType.TAGDAT)
                .orElseThrow(() -> new ScheduleException(
                        (LogStringUtil.of("mailCsvFile").message("velocity error.").build())));

        // BCCメールアドレスを再取得
        final String bccMaileAddress = firstEntity.getEmail();

        // メール送信情報
        final String fromMailAddress = propertyComponent.getBatchProperty().getTagdatProperty().getMailFrom();
        final String toMailAddress = propertyComponent.getBatchProperty().getTagdatProperty().getMailTo();
        final String ccMailAddress = propertyComponent.getBatchProperty().getTagdatProperty().getMailCc();
        final String bccMailAddress = bccMaileAddress;
        final String subject = optionalMailModel.getTitle();
        final String messageBody = optionalMailModel.getBody();

        final List<MailAttachementFileModel> attachementFiles = new ArrayList<MailAttachementFileModel>();
        brands.stream().forEach(brand -> {
	        File file = new File(propertyComponent.getBatchProperty().getTagdatProperty().getTmpDirectory(),
	    			TMP_FILE_PREFIX_INFO + brand + TMP_FILE_SUFFIX_INFO);
	        if (file.exists()) {
	        	final MailAttachementFileModel model = new MailAttachementFileModel();
	        	model.setFile(file);
	        	model.setFileName(file.getName());
	        	attachementFiles.add(model);
	        }
        });

        if (attachementFiles.isEmpty()) {
            // 添付ファイル0件の場合、警告ログを出力しメールは送信しない
            log.warn(LogStringUtil.of("execute").message("There are no attachments.").value("fromMailAddress", fromMailAddress)
            		.value("toMailAddress", toMailAddress).value("ccMailAddress", ccMailAddress).value("bccMailAddress", bccMailAddress)
                    .value("subject", subject).value("messageBody", messageBody).build());
            return;
        }

    	// メール情報をログ出力
    	log.info((LogStringUtil.of("mailCsvFile").message("mail info.").value("fromMailAddress", fromMailAddress)
                .value("toMailAddress", toMailAddress).value("ccMailAddress", ccMailAddress).value("bccMailAddress", bccMailAddress)
                .value("subject", subject).value("messageBody", messageBody).build()));

    	// メール送信
        final SendMailType sendType = mailSenderAttachmentComponent.sendMail(fromMailAddress, toMailAddress, ccMailAddress, bccMailAddress, subject,
                messageBody, attachementFiles);
        if (sendType == SendMailType.MAIL_SENDING_ERROR) {
        	// メール情報をエラーログ出力する
        	log.error((LogStringUtil.of("mailCsvFile").message("mail send error.").value("fromMailAddress", fromMailAddress)
                    .value("toMailAddress", toMailAddress).value("ccMailAddress", ccMailAddress).value("bccMailAddress", bccMailAddress)
                    .value("subject", subject).value("messageBody", messageBody).build()));

        	// 送信ステータスを「送信エラー(2)」に更新
        	brands.stream().forEach(brand -> {
        		updateSendStatus(ERROR, brand, userId);
            });
        }
    }


    /**
     * 処理対象のTAGDATメール送信情報のステータスを更新.
     * @param status 送信エラー(2)
     * @param brkg ブランドコード
     * @param userId ユーザID
     */
    private void updateSendStatus(final int status, final String brkg, final BigInteger userId) {
    	tagdatRepository.updateSendStatus(status, brkg, userId);
    }
}
