package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.DeliveryAtLateType;
import jp.co.jun.edi.type.OrderApprovalType;
import jp.co.jun.edi.type.QualityApprovalType;
import lombok.Data;

/**
 * デリスケ納品依頼情報ViewのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VDelischeDeliveryRequestModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 発注ID. */
    private BigInteger orderId;

    /** 納品ID. */
    private BigInteger deliveryId;

    /** 納品依頼回数. */
    private Integer deliveryCount;

    /** 納期. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryAt;

    /** 月度. */
    private Integer deliveryAtMonthly;

    /** ブランド. */
    private String brandCode;

    /** アイテム. */
    private String itemCode;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** シーズン. */
    private String season;

    /** メーカーコード. */
    private String mdfMakerCode;

    /** メーカー名. */
    private String mdfMakerName;

    /** 製品発注日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date productOrderAt;

    /** 製品納期. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date productDeliveryAt;

    /** 納期遅延フラグ. */
    private DeliveryAtLateType lateDeliveryAtFlg;

    /** 発注数合計. */
    private Integer productOrderLotSum;

    /** 納品依頼数合計. */
    private Integer deliveryLotSum;

    /** 入荷数合計. */
    private Integer arrivalLotSum;

    /** 上代合計. */
    private BigDecimal calculateRetailPrice;

    /** 原価合計. */
    private BigDecimal calculateProductCost;

    /** 発注承認ステータス. */
    private OrderApprovalType orderApproveStatus;

    /** 優良誤認承認区分（組成）. */
    private QualityApprovalType qualityCompositionStatus;

    /** 優良誤認承認区分（国）. */
    private QualityApprovalType qualityCooStatus;

    /** 優良誤認承認区分（有害物質）. */
    private QualityApprovalType qualityHarmfulStatus;
}
