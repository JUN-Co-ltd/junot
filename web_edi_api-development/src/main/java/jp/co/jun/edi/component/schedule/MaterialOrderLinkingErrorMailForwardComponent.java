package jp.co.jun.edi.component.schedule;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.S3Component;
import jp.co.jun.edi.component.mail.MailSenderComponent;
import jp.co.jun.edi.component.mail.VelocityMailTemplateComponent;
import jp.co.jun.edi.component.model.MaterialOrderLinkingErrorMailForwardCsvModel;
import jp.co.jun.edi.component.model.VelocityConvertedMailTemplateModel;
import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.entity.TFOrderEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.model.mail.MaterialOrderLinkingErrorMailForwardModel;
import jp.co.jun.edi.repository.MUserRepository;
import jp.co.jun.edi.repository.TFOrderRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderRepository;
import jp.co.jun.edi.type.FukukitaruMasterConfirmStatusType;
import jp.co.jun.edi.type.FukukitaruMasterLinkingStatusType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.util.BusinessUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 資材発注連携エラーメール転送用コンポーネント.
 */
@Slf4j
@Component
public class MaterialOrderLinkingErrorMailForwardComponent {
    /** CSVヘッダー項目. */
    private static final String[] CSV_HEADER = {"オーダー識別コード", "受信日時", "取込日時", "エラーコード", "エラーメッセージ"};
    private static final int NUMBER_0 = 0;
    private static final int NUMBER_1 = 1;
    private static final int NUMBER_2 = 2;
    private static final int NUMBER_3 = 3;
    private static final int NUMBER_4 = 4;

    @Autowired
    private S3Component s3Component;
    @Autowired
    private PropertyComponent property;
    @Autowired
    private TFOrderRepository tFOrderRepository;
    @Autowired
    private TItemRepository tItemRepository;
    @Autowired
    private MailSenderComponent mailSenderComponent;
    @Autowired
    private ExtendedTOrderRepository extendedTOrderRepository;
    @Autowired
    private MUserRepository mUserRepository;
    @Autowired
    private VelocityMailTemplateComponent<MaterialOrderLinkingErrorMailForwardModel> velocityMailTemplateComponent;

    /**
     * S3からバッチ処理対象のフクキタルエラーメールのオブジェクト概要のリストを取得する.
     *
     * @return オブジェクト概要のリスト（オブジェクトのデータは含まれていない）
     */
    public List<S3ObjectSummary> getObjectSummaries() {
        return s3Component.getObjectSummaries(property.getBatchProperty().getMaterialOrderLinkingErrorMailForward().getReceivedPrefix());
    }

    /**
     * S3から資材発注連携エラーメールのオブジェクトをダウンロードし、 オーダー識別コード別にエラーメールを転送する.
     *
     * @param key
     *            S3の格納先のキー
     * @param targetOrderCode
     *            実行対象となるオーダー識別コード. NULLまたは空データの場合は指定なし.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void mailForward(final String key, final List<String> targetOrderCode) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(LogStringUtil.of("getContentsAndMailForward").value("key", key).build());
            }

            // メール送信失敗回数
            final List<String> failureOrderCode = new ArrayList<String>();

            // オーダー識別コードごとにグルーピング
            final Map<String, List<MaterialOrderLinkingErrorMailForwardCsvModel>> groupOrderCodeList = parse(s3Component.download(key)).stream()
                    .collect(Collectors.groupingBy(MaterialOrderLinkingErrorMailForwardCsvModel::getOrderCode));

            // オーダー識別コードごとにエラーメールを転送する
            for (Entry<String, List<MaterialOrderLinkingErrorMailForwardCsvModel>> entry : groupOrderCodeList.entrySet()) {
                final String orderCode = entry.getKey();
                try {
                    // オーダー識別コードの指定がある場合、処理対象が指定のオーダー識別コードか確認する
                    if (CollectionUtils.isNotEmpty(targetOrderCode)) {
                        if (!targetOrderCode.contains(orderCode)) {
                            // 対象のオーダー識別コードではない場合、次の処理を実行する
                            continue;
                        }
                    }
                    forward(key, groupOrderCodeList.get(orderCode), orderCode);

                } catch (Exception e) {
                    // エラーログ出力
                    log.error(e.getMessage(), e);
                    // 失敗したオーダー識別コードを詰め込む
                    failureOrderCode.add(orderCode);
                }
            }

            // S3のメール情報を移動する
            if (CollectionUtils.isEmpty(failureOrderCode)) {
                // 全ての処理が成功している場合、S3のメール情報を、転送済フォルダへ移動する
                final String receivePrefix = property.getBatchProperty().getMaterialOrderLinkingErrorMailForward().getReceivedPrefix();
                final String forwardedPrefix = property.getBatchProperty().getMaterialOrderLinkingErrorMailForward().getForwardedPrefix();
                final String destinationKey = StringUtils.replace(key, receivePrefix, forwardedPrefix);
                s3Component.moveObject(key, destinationKey);

            } else {
                // 1件以上処理が失敗している場合、S3のメール情報を、エラーフォルダへ移動する
                throw new ScheduleException(LogStringUtil.of("getContentsAndMailForward").message("Material order linkage error mail transfer some error.")
                        .value("error order_code list", failureOrderCode).build());
            }

        } catch (Exception e) {
            // メールをエラーフォルダに移動
            final String receivePrefix = property.getBatchProperty().getMaterialOrderLinkingErrorMailForward().getReceivedPrefix();
            final String errorPrefix = property.getBatchProperty().getMaterialOrderLinkingErrorMailForward().getErrorPrefix();
            final String destinationKey = StringUtils.replace(key, receivePrefix, errorPrefix);
            s3Component.moveObject(key, destinationKey);
            log.error(LogStringUtil.of("getContentsAndMailForward").message(e.getMessage()).value("s3.key", key).value("moved s3.key", destinationKey).build(),
                    e);
        }
    }

    /**
     * メール転送処理.
     *
     * @param key
     *            S3キー
     * @param csvModelList
     *            {@link List<MaterialOrderLinkingErrorMailForwardCsvModel>} リスト
     * @param orderCode
     *            オーダー識別コード
     * @throws Exception
     *             例外
     */
    private void forward(final String key, final List<MaterialOrderLinkingErrorMailForwardCsvModel> csvModelList, final String orderCode) throws Exception {
        // 管理者ユーザ情報取得
        final MUserEntity adminUser = mUserRepository
                .findByAccountNameAndCompanyIgnoreSystemManaged(property.getCommonProperty().getAdminUserAccountName(),
                        property.getCommonProperty().getAdminUserCompany())
                .orElseThrow(() -> new ScheduleException(
                        LogStringUtil.of("forward").message("m_user not found .").value("accountName", property.getCommonProperty().getAdminUserAccountName())
                                .value("company", property.getCommonProperty().getAdminUserCompany()).value("order_code", orderCode).build()));

        // オーダー識別コードから資材発注情報を取得する
        final TFOrderEntity tfOrderEntity = tFOrderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ScheduleException(LogStringUtil.of("forward").message("t_f_order not found.").value("order_code", orderCode).build()));
        // 資材発注情報から品番情報を取得する
        final TItemEntity tItemEntity = tItemRepository.findByIdAndDeletedAtIsNull(tfOrderEntity.getPartNoId())
                .orElseThrow(() -> new ScheduleException(LogStringUtil.of("forward")
                        .message("t_item not found.").value("t_item.id", tfOrderEntity.getPartNoId())
                        .value("order_code", orderCode).build()));
        // 資材発注情報から発注情報を取得する
        final ExtendedTOrderEntity exTOrderEntity = extendedTOrderRepository.findById(tfOrderEntity.getOrderId())
                .orElseThrow(() -> new ScheduleException(LogStringUtil.of("forward")
                        .message("t_item not found.").value("t_item.id", tfOrderEntity.getPartNoId())
                        .value("order_code", orderCode).build()));
        // 発注者ユーザIDからユーザ情報を取得する
        final MUserEntity mUserEntity = mUserRepository.findByIdAndEnabledTrueAndDeletedAtIsNull(tfOrderEntity.getOrderUserId())
                .orElseThrow(() -> new ScheduleException(LogStringUtil.of("forward").message("m_user not found.")
                        .value("m_user.id", tfOrderEntity.getOrderUserId()).value("order_code", orderCode).build()));

        // 転送完了した資材発注情報の連携ステータスを「連携エラー(9)」」に変更する
        tFOrderRepository.updateLinkingStatus(FukukitaruMasterLinkingStatusType.NON_TARGET, tfOrderEntity.getId(), adminUser.getId());

        // メールアドレスが存在しない場合、エラーとする
        if (StringUtils.isEmpty(mUserEntity.getMailAddress())) {
            throw new ScheduleException(LogStringUtil.of("forward").message("m_user does not have an email address.").value("userId", mUserEntity.getId())
                    .value("order_code", orderCode).build());
        }
        // メール送信フラグ
        if (property.getBatchProperty().getMaterialOrderLinkingErrorMailForward().isMailSend()) {
            // メールデータを生成
            final MaterialOrderLinkingErrorMailForwardModel sendModel = new MaterialOrderLinkingErrorMailForwardModel();
            sendModel.setMessages(csvModelList);
            sendModel.setPartNoId(tItemEntity.getId());
            sendModel.setOrderId(exTOrderEntity.getId());
            sendModel.setFOrderId(tfOrderEntity.getId());
            sendModel.setOrderAt(tfOrderEntity.getOrderAt());
            sendModel.setOrderCode(tfOrderEntity.getOrderCode());
            sendModel.setOrderNo(exTOrderEntity.getOrderNumber());
            sendModel.setPartNo(BusinessUtils.formatPartNo(tItemEntity.getPartNo()));
            sendModel.setPreferredShippingAt(tfOrderEntity.getPreferredShippingAt());
            sendModel.setProductName(tItemEntity.getProductName());
            sendModel.setSire(exTOrderEntity.getMdfMakerName());
            sendModel.setUri(getUri(tfOrderEntity.getOrderType()));
            sendModel.setUrl(property.getCommonProperty().getJunotUrl());
            sendModel.setSubjectPrefix(property.getCommonProperty().getSendMailTemplateEmbeddedCharacterSubjectPrefix());
            final VelocityConvertedMailTemplateModel velocityModel = velocityMailTemplateComponent
                    .convert(sendModel, MMailCodeType.MATERIAL_ORDER_LINKING_ERROR_MAIL_FORWARD)
                    .orElseThrow(() -> new ScheduleException(LogStringUtil.of("forward").message("velocity error.").value("order_code", orderCode).build()));

            // 発注者ユーザIDに該当するユーザに対してメールを送信する
            final String fromMailAddress = property.getBatchProperty().getMaterialOrderLinkingErrorMailForward().getMailFrom();
            final String toMailAddress = mUserEntity.getMailAddress();
            final String ccMailAddress = property.getBatchProperty().getMaterialOrderLinkingErrorMailForward().getMailCc();
            final String bccMailAddress = property.getBatchProperty().getMaterialOrderLinkingErrorMailForward().getMailBcc();
            final String subject = velocityModel.getTitle();
            final String messageBody = velocityModel.getBody();

            // 製造発注情報の生産メーカーに転送する
            mailSenderComponent.sendMail(fromMailAddress, toMailAddress, ccMailAddress, bccMailAddress, subject, messageBody);
            log.info(LogStringUtil.of("forward").message("Material order linkage error mail transfer success.").value("s3.key", key)
                    .value("order_code", orderCode).value("toMailAddress", toMailAddress).value("subject", subject).value("messageBody", messageBody).build());
        } else {
            log.warn(LogStringUtil.of("forward").message("Invalid email sending setting").value("order_code", orderCode).build());
        }

        // 転送完了した資材発注情報の連携ステータスを「未連携(0)」、確定ステータスを「未確定(0)」に変更する
        tFOrderRepository.updateLinkingStatusAndConfirmStatus(FukukitaruMasterLinkingStatusType.TARGET, FukukitaruMasterConfirmStatusType.ORDER_NOT_CONFIRMED,
                tfOrderEntity.getId(), adminUser.getId());

    }

    /**
     * URI取得.
     * @param orderType 資材発注種別.
     * @return URI
     */
    private String getUri(final FukukitaruMasterOrderType orderType) {
        String uri = "";
        switch (orderType) {
        case WASH_NAME:
        case WASH_NAME_KOMONO:
            uri = "fukukitaruOrder01Wash";
            break;
        case HANG_TAG:
        case HANG_TAG_KOMONO:
            uri = "fukukitaruOrder01HangTag";
            break;
        default:
            break;
        }
        return uri;
    }

    /**
     * メール本文を解析して、1件ごとのエラー情報に変換する.
     *
     * @param object
     *            S3から取得したオブジェクトのバイト配列
     * @return エラー情報のリスト
     * @throws Exception
     *             メールの解析に失敗した場合
     */
    private List<MaterialOrderLinkingErrorMailForwardCsvModel> parse(final byte[] object) throws Exception {
        final Session session = Session.getDefaultInstance(new java.util.Properties(), null);
        try (ByteArrayInputStream is = new ByteArrayInputStream(object)) {
            final MimeMessage mimeMessage = new MimeMessage(session, is);
            // MimeTypeから添付ファイル有無を判定する
            if (mimeMessage.isMimeType("multipart/*")) {
                final MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();
                final int count = mimeMultipart.getCount();
                for (int i = 0; i < count; i++) {
                    if (mimeMultipart.getBodyPart(i).isMimeType("application/octet-stream")) {
                        // CSVデータ
                        return readCsvData(mimeMultipart, i);
                    }
                }
            }

            // 添付ファイルが見つからない
            throw new ScheduleException(LogStringUtil.of("parse").message("attachment not found.").build());
        }
    }

    /**
     * CSVデータを読み込む.
     *
     * @param mimeMultipart
     *            MIMEMultipart
     * @param index
     *            MIMEMultipartのインデックス
     * @return CSVデータ
     * @throws Exception
     *             例外
     */
    private List<MaterialOrderLinkingErrorMailForwardCsvModel> readCsvData(final MimeMultipart mimeMultipart, final int index) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(mimeMultipart.getBodyPart(index).getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser parser = CSVFormat.DEFAULT // ExcelのCSV形式を指定
                    .withIgnoreEmptyLines(false) // 空行を無視する
                    .withHeader(CSV_HEADER) // ヘッダの指定
                    .withFirstRecordAsHeader() // 最初の行をヘッダーとして読み飛ばす
                    .withIgnoreSurroundingSpaces(true) // 値をtrimして取得する
                    .withRecordSeparator("\n") // 改行コードLF
                    .withDelimiter(',') // 区切りカンマ
                    .withQuote('"').parse(br);

            // CSVデータをモデルに変換する
            final List<MaterialOrderLinkingErrorMailForwardCsvModel> list = parser.getRecords().stream().map(csvRecord -> {
                final MaterialOrderLinkingErrorMailForwardCsvModel model = new MaterialOrderLinkingErrorMailForwardCsvModel();
                final String orderCode = csvRecord.get(CSV_HEADER[NUMBER_0]);
                model.setOrderCode(orderCode);
                model.setReceivedAt(csvRecord.get(CSV_HEADER[NUMBER_1]));
                model.setTakeInAt(csvRecord.get(CSV_HEADER[NUMBER_2]));
                model.setErrorCode(csvRecord.get(CSV_HEADER[NUMBER_3]));
                model.setErrorMessage(csvRecord.get(CSV_HEADER[NUMBER_4]));
                return model;
            }).collect(Collectors.toList());
            parser.close();

            return list;
        }
    }
}
