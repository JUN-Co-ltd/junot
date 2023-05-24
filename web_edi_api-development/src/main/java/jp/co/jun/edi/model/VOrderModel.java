package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.AutoType;
import jp.co.jun.edi.type.CompleteOrderType;
import jp.co.jun.edi.type.CompleteType;
import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.OrderSheetOutType;
import lombok.Data;

/**
 * 発注情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VOrderModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 品番. */
    private String partNo;

    /** 費目. */
    private ExpenseItemType expenseItem;

    /** 裁断自動区分. */
    private AutoType cutAutoType;

    /** 生地メーカー. */
    private String matlMakerCode;

    /** 生地品番. */
    private String matlPartNo;

    /** 生地品名. */
    private String matlProductName;

    /** 生地納期. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date matlDeliveryAt;

    /** 生地m数. */
    private BigDecimal matlMeter;

    /** 生地単価. */
    private BigDecimal matlUnitPrice;

    /** 反番. */
    private String clothNumber;

    /** 実用尺. */
    private BigDecimal necessaryLengthActual;

    /** 用尺単位. */
    private String necessaryLengthUnit;

    /** 生地原価. */
    private BigDecimal matlCost;

    /** 生産メーカー. */
    private String mdfMakerCode;

    /** 生産工場. */
    private String mdfMakerFactoryCode;

    /** 生産工場名. */
    private String mdfMakerFactoryName;

    /** 製品発注日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date productOrderAt;

    /** 製品納期. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date productDeliveryAt;

    /** 製品修正納期. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date productCorrectionDeliveryAt;

    /** 発注完了日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date orderCompleteAt;

    /** 製造担当コード. */
    private String mdfStaffCode;

    /** 製品完納区分. */
    private CompleteOrderType productCompleteOrder;

    /** 製品済区分. */
    private CompleteType productCompleteType;

    /** 全済区分. */
    private CompleteType allCompletionType;

    /** 数量. */
    private BigDecimal quantity;

    /** 単価. */
    private BigDecimal unitPrice;

    /** 上代. */
    private BigDecimal retailPrice;

    /** 製品原価. */
    private BigDecimal productCost;

    /** 加工賃. */
    private BigDecimal processingCost;

    /** 附属代. */
    private BigDecimal attachedCost;

    /** その他原価. */
    private BigDecimal otherCost;

    /** B級品単価. */
    private BigDecimal nonConformingProductUnitPrice;

    /** 輸入区分. */
    private String importCode;

    /** 原産国コード. */
    private String cooCode;

    /** 摘要. */
    private String application;

    /** 送信区分. */
    private String sendCode;

    /** 発注承認ステータス. */
    private String orderApproveStatus;

    /** 発注確定日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date orderConfirmAt;

    /** 発注確定者. */
    private BigInteger orderConfirmUserId;

    /** 発注承認日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date orderApproveAt;

    /** 発注承認者. */
    private BigInteger orderApproveUserId;

    /** 発注書出力フラグ. */
    private OrderSheetOutType orderSheetOut;

    /** 連携登録者. */
    private String junpcTanto;

    /** 連携ステータス. */
    private LinkingStatusType linkingStatus;

    /** 連携日時. */
    private Date linkedAt;

    /** 発注SKU情報のリスト. */
    private List<OrderSkuModel> orderSkus;

    /** 品名. */
    private String productName;

    /** 年度. */
    private Integer year;

    /** サブシーズン. */
    private String subSeasonCode;

    /** ブランド. */
    private String brandCode;

    /** ブランドソートコード. */
    private String brandSortCode;

    /** アイテムコード. */
    private String itemCode;

    /** 部門コード. */
    private String deptCode;

    /** 希望納品日. */
    private String preferredDeliveryDate;

    /** プランナーコード. */
    private String plannerCode;

    /** パタンナーコード. */
    private String patanerCode;

    /** 優良誤認区分. */
    private String misleadingRepresentation;

    /** 優良誤認承認区分（組成）. */
    private String qualityCompositionStatus;

    /** 優良誤認承認区分（国）. */
    private String qualityCooStatus;

    /** 優良誤認承認区分（有害物質）. */
    private String qualityHarmfulStatus;

    /** 納品依頼View情報.
     *  ListのAPIの返却値はこちらにセットされる
     * */
    private List<VDeliveryModel> deliverys;

    /** 生産メーカー名. */
    private String mdfMakerName;

    /** 納品予定ID. */
    private BigInteger deliveryPlanId;

    /** 納品予定明細件数. */
    private int deliveryPlanDetailsCnt;

    /** 生産ステータス件数. */
    private int productionStatusCnt;

    /** 発注ファイル情報. */
    private OrderFileInfoModel orderFileInfo;

    /** フクキタル資材発注利用可能フラグ(true:利用可能、false:利用不可能). */
    private boolean materialOrderAvailable;

    /** フクキタル資材発注確定1件以上存在する(true:利用可能、false:利用不可能). */
    private boolean existsMaterialOrderConfirm;

    /** 登録ステータス. */
    private int registStatus;
}
