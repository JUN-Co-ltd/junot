package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.DelischeProductionStatusType;
import jp.co.jun.edi.type.OrderApprovalType;
import jp.co.jun.edi.type.QualityApprovalType;
import lombok.Data;

/**
 * デリスケ発注情報ViewのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VDelischeOrderModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID(発注ID). */
    private BigInteger id;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 製品納期. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date productDeliveryAt;

    /** 月度. */
    private Integer productDeliveryAtMonthly;

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

    // PRD_0146 #10776 add JFE start
    /** 費目. */
    private String expenseItem;

    /** 関連No. */
    private BigInteger relationNumber;
    // PRD_0146 #10776 add JFE end

    /** メーカーコード. */
    private String mdfMakerCode;

    /** メーカー名. */
    private String mdfMakerName;

    /** 製品発注日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date productOrderAt;

    /** 生産工程区分. */
    private DelischeProductionStatusType productionStatus;

    /** 納期遅延件数. */
    private Integer lateDeliveryAtCnt;

    /** 製品発注数. */
    private Integer quantity;

    /** 納品依頼数合計. */
    private Integer deliveryLotSum;

    /** 入荷数合計(仕入実数). */
    private Integer arrivalLotSum;

    /** 純売上数. */
    private Integer netSalesQuantity;

    /** 在庫数. */
    private Integer stockQuantity;

    /** 上代. */
    private BigDecimal retailPrice;

    /** 原価. */
    private BigDecimal productCost;

    /** 発注残. */
    private Integer orderRemainingLot;

    /** 上代合計. */
    private BigDecimal calculateRetailPrice;

    /** 原価合計. */
    private BigDecimal calculateProductCost;

    /** 原価率. */
    private double costRate;

    /** 製品完納区分. */
    private String productCompleteOrder;

    /** 子要素存在フラグ. */
    private boolean childExists;

    /** 発注承認ステータス. */
    private OrderApprovalType orderApproveStatus;

    /** 優良誤認承認区分（組成）. */
    private QualityApprovalType qualityCompositionStatus;

    /** 優良誤認承認区分（国）. */
    private QualityApprovalType qualityCooStatus;

    /** 優良誤認承認区分（有害物質）. */
    private QualityApprovalType qualityHarmfulStatus;
}
