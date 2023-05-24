package jp.co.jun.edi.component.mail;

import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.model.CommonPropertyModel;
import jp.co.jun.edi.component.model.VelocityConvertedMailTemplateModel;
import jp.co.jun.edi.entity.TOrderApprovalSendMailEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.model.mail.OrderApprovedSendModel;
import jp.co.jun.edi.repository.TOrderApprovalSendMailRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.type.SendMailStatusType;

/**
 *
 * 発注書PDF発行[即時] PDF発行・メール送信用データ作成用コンポーネント.
 *
 */
@Component
public class StackTOrderSendMailComponent {
    @Autowired
    private TOrderApprovalSendMailRepository tOrderApprovalSendMailRepository;

    @Autowired
    private VelocityMailTemplateComponent<OrderApprovedSendModel> velocityMailTemplateComponent;

    @Autowired
    private MailAddressComponent mailAddressComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    /**
     *発注書PDF発行[即時] PDF発行キューとメール送信用データ作成.
     *
     * @param extendedTOrderEntity 拡張発注情報
     * @param extendedTItemEntity 拡張品番情報
     * @param customLoginUser ログインユーザ情報
     * @return データ作成に成功した場合trueを返す
     */
    public boolean saveOrderSendMailData(final ExtendedTOrderEntity extendedTOrderEntity,
                                           final ExtendedTItemEntity extendedTItemEntity,
                                           final CustomLoginUser customLoginUser) {

        final TOrderApprovalSendMailEntity tOrderApprovalSendMailEntity = new TOrderApprovalSendMailEntity();

        // 発注ID
        tOrderApprovalSendMailEntity.setOrderId(extendedTOrderEntity.getId());
        // 状態
        tOrderApprovalSendMailEntity.setStatus(SendMailStatusType.UNPROCESSED);

        // メールアドレス取得
        final String bccMailAddress = getSendMailaddress(extendedTOrderEntity.getMdfStaffCode(),
                                                            extendedTOrderEntity.getMdfMakerCode(),
                                                            customLoginUser.getAccountName());
        // BCC宛先が存在しない場合は、PDF生成のみ実行するステータスを設定しキューに入れる
        if (StringUtils.isEmpty(bccMailAddress)) {
            tOrderApprovalSendMailEntity.setCreatedOnlyPdf(BooleanType.TRUE);
            tOrderApprovalSendMailRepository.save(tOrderApprovalSendMailEntity);
            return true;
        }

        // メールテンプレートからメール内容を生成する
        final OrderApprovedSendModel sendModel = generateSendMailData(extendedTOrderEntity, extendedTItemEntity);
        final Optional<VelocityConvertedMailTemplateModel> optionalMailModel = velocityMailTemplateComponent.convert(sendModel,
                MMailCodeType.PURCHASE_ORDER_IMMEDIATE);
        if (!optionalMailModel.isPresent()) {
            // メール内容の取得失敗の場合、falseを返す
            return false;
        }

        // メール送信情報をキューに入れる
        final VelocityConvertedMailTemplateModel mailModel = optionalMailModel.get();
        final CommonPropertyModel commonPropertyModel = propertyComponent.getCommonProperty();
        tOrderApprovalSendMailEntity.setCreatedOnlyPdf(BooleanType.FALSE);
        tOrderApprovalSendMailEntity.setFromMailAddress(commonPropertyModel.getSendMailFrom());
        tOrderApprovalSendMailEntity.setToMailAddress(commonPropertyModel.getSendMailTo());
        tOrderApprovalSendMailEntity.setCcMailAddress(commonPropertyModel.getSendMailCc());
        tOrderApprovalSendMailEntity.setBccMailAddress(bccMailAddress);
        tOrderApprovalSendMailEntity.setSubject(mailModel.getTitle());
        tOrderApprovalSendMailEntity.setMessageBody(mailModel.getBody());

        tOrderApprovalSendMailRepository.save(tOrderApprovalSendMailEntity);

        return true;
    }

    /**
     * PDF発行キュー・メール送信用データ作成.
     * @param extendedTOrderEntity 拡張発注情報
     * @param extendedTItemEntity 拡張品番情報
     * @return OrderApprovedSendModel
     */
    private OrderApprovedSendModel generateSendMailData(final ExtendedTOrderEntity extendedTOrderEntity, final ExtendedTItemEntity extendedTItemEntity) {
        final OrderApprovedSendModel orderApprovedSendModel = new OrderApprovedSendModel();

        // 品番
        orderApprovedSendModel.setPartNo(extendedTItemEntity.getPartNo());
        // 品名
        orderApprovedSendModel.setProductName(extendedTItemEntity.getProductName());
        // 生産メーカーコード
        orderApprovedSendModel.setMdfMakerCode(extendedTOrderEntity.getMdfMakerCode());
        // 生産メーカー名
        orderApprovedSendModel.setMdfMakerName(extendedTOrderEntity.getMdfMakerName());
        // 発注No
        orderApprovedSendModel.setOrderNumber(extendedTOrderEntity.getOrderNumber());
        // 発注数
        orderApprovedSendModel.setQuantity(extendedTOrderEntity.getQuantity());
        // 納期(修正納期)
        orderApprovedSendModel.setProductDeliveryAt(extendedTOrderEntity.getProductCorrectionDeliveryAt());
        // 発注ID
        orderApprovedSendModel.setOrderId(extendedTOrderEntity.getId());
        // URL
        orderApprovedSendModel.setUrl(propertyComponent.getCommonProperty().getJunotUrl());
        // 件名接頭辞
        orderApprovedSendModel.setSubjectPrefix(propertyComponent.getCommonProperty().getSendMailTemplateEmbeddedCharacterSubjectPrefix());
        // 企画担当
        orderApprovedSendModel.setPlannerCode(extendedTItemEntity.getPlannerCode());
        // 製造担当
        orderApprovedSendModel.setMdfStaffCode(extendedTOrderEntity.getMdfStaffCode());
        // パターンナー
        orderApprovedSendModel.setPlannerCode(extendedTItemEntity.getPlannerCode());
        // 生産メーカー担当：生産メーカー担当宛にはメールを送信しないため、値はセットしない。

        return orderApprovedSendModel;

    }


    /**
     * メール送信先のメールアドレスを取得する.
     *
     * @param mdfStaffCode     製造担当コード
     * @param supplierCode メーカーコード
     * @param loginAccoutName  ログインユーザのアカウント名
     * @return カンマ区切りで結合されたメールアドレスの文字列
     */
    private String getSendMailaddress(final String mdfStaffCode, final String supplierCode, final String loginAccoutName) {


        // JUNの担当メールアドレスを取得
        final String[] junMailTo = mailAddressComponent.getJunMailaddressArray(mdfStaffCode, loginAccoutName);

        // 生産メーカーに紐づくメールアドレスを取得
        String[] makerMailTo = mailAddressComponent.getAllMakerAccountMailaddress(supplierCode);


        return mailAddressComponent.joinMailAddressList(ArrayUtils.addAll(makerMailTo, junMailTo));
    }
}
