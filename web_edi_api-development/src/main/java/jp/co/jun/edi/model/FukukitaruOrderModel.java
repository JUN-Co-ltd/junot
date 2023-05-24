package jp.co.jun.edi.model;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.FukukitaruMasterConfirmStatusType;
import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.FukukitaruMasterLinkingStatusType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * フクキタル発注情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class FukukitaruOrderModel extends GenericModel {
    private static final long serialVersionUID = 1L;

    /** フクキタル発注:フクキタル発注ID. */
    private BigInteger id;

    /** フクキタル発注:フクキタル品番ID. */
    private BigInteger fItemId;

    /** フクキタル発注:承認需要フラグ. */
    private BooleanType isApprovalRequired;

    /** フクキタル発注:請求先ID. */
    private BigInteger billingCompanyId;

    /** フクキタル発注:請求先住所. */
    private FukukitaruDestinationModel billingDestination;

    /** フクキタル発注:契約No. */
    private String contractNumber;

    /** フクキタル発注:納入先ID. */
    private BigInteger deliveryCompanyId;

    /** フクキタル発注:納入先住所. */
    private FukukitaruDestinationModel deliveryDestination;

    /** フクキタル発注:納入先担当者. */
    private String deliveryStaff;

    /** フクキタル発注:工場No. */
    private String mdfMakerFactoryCode;

    /** フクキタル発注:発注日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date orderAt;

    /** フクキタル発注:オーダー識別コード. */
    private String orderCode;

    /** フクキタル発注:発注者コード. */
    private BigInteger orderUserId;

    /** フクキタル発注:希望出荷日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date preferredShippingAt;

    /** フクキタル発注:リピート数. */
    private Integer repeatNumber;

    /** フクキタル発注:特記事項. */
    private String specialReport;

    /** フクキタル発注:備考. */
    private String remarks;

    /**
     * フクキタル発注:緊急. false:緊急ではない(0)、true:緊急(1)
     */
    private BooleanType urgent;

    /** フクキタル発注:手配先. */
    private FukukitaruMasterDeliveryType deliveryType;

    /** フクキタル発注:確定ステータス. */
    private FukukitaruMasterConfirmStatusType confirmStatus;

    /** 発注送信日. */
    private Date orderSendAt;

    /** フクキタル発注:洗濯ネーム(1). */
    private List<FukukitaruOrderSkuModel> orderSkuWashName;

    /** フクキタル発注:アテンションネーム(2). */
    private List<FukukitaruOrderSkuModel> orderSkuAttentionName;

    /** フクキタル発注:洗濯同封副資材(3). */
    private List<FukukitaruOrderSkuModel> orderSkuWashAuxiliary;

    /** フクキタル発注:下札(4). */
    private List<FukukitaruOrderSkuModel> orderSkuBottomBill;

    /** フクキタル発注:アテンションタグ(5). */
    private List<FukukitaruOrderSkuModel> orderSkuAttentionTag;

    /** フクキタル発注:アテンション下札(6). */
    private List<FukukitaruOrderSkuModel> orderSkuBottomBillAttention;

    /** フクキタル発注:NERGY用メリット下札(7). */
    private List<FukukitaruOrderSkuModel> orderSkuBottomBillNergyMerit;

    /** フクキタル発注:下札同封副資材(8). */
    private List<FukukitaruOrderSkuModel> orderSkuBottomBillAuxiliaryMaterial;

    /** 発注種別. */
    private FukukitaruMasterOrderType orderType;

    /**
     * 責任発注. false:メーカー責任発注ではない(0)、true:メーカー責任発注(1)
     */
    private BooleanType isResponsibleOrder;

    /** 連携ステータス. */
    private FukukitaruMasterLinkingStatusType linkingStatus;

    // *************************************************************/
    // フクキタル発注情報拡張
    // *************************************************************/
    /** 合計発注数. */
    private int totalOrderLot;

    // *************************************************************/
    // フクキタル品番情報
    // *************************************************************/
    /** フクキタル品番情報. */
    private FukukitaruItemModel fkItem;

    // *************************************************************/
    // 発注情報
    // *************************************************************/
    /** 発注情報:発注ID. */
    private BigInteger orderId;

    /** 発注情報:発注No. */
    private BigInteger orderNumber;

    /** 発注情報:製品修正納期. */
    private Date productCorrectionDeliveryAt;

    // *************************************************************/
    // 品番情報
    // *************************************************************/
    /** 品番情報:品番ID. */
    private BigInteger partNoId;

    /** 品番情報:品番. */
    private String partNo;

    /** 品番情報:品名. */
    private String productName;

    /** 品番情報:生産メーカー名. */
    private String mdfMakerName;
}
