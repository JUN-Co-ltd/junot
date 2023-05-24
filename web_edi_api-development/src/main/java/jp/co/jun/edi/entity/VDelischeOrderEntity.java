package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.DelischeProductionStatusTypeConverter;
import jp.co.jun.edi.entity.converter.OrderApprovalTypeConverter;
import jp.co.jun.edi.entity.converter.QualityApprovalTypeConverter;
import jp.co.jun.edi.type.DelischeProductionStatusType;
import jp.co.jun.edi.type.OrderApprovalType;
import jp.co.jun.edi.type.QualityApprovalType;
import lombok.Data;

/**
 * デリスケ発注情報ViewのEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class VDelischeOrderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID(発注ID). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 製品納期. */
    @Column(name = "product_delivery_at")
    private Date productDeliveryAt;

    /** 月度. */
    @Column(name = "product_delivery_at_monthly")
    private Integer productDeliveryAtMonthly;

    /** ブランド. */
    @Column(name = "brand_code")
    private String brandCode;

    /** アイテム. */
    @Column(name = "item_code")
    private String itemCode;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** シーズン. */
    private String season;

    // PRD_0146 #10776 add JFE start
    /** 費目. */
    @Column(name = "expense_item")
    private String expenseItem;

    /** 関連No. */
    @Column(name = "relation_number")
    private BigInteger relationNumber;
    // PRD_0146 #10776 add JFE end

    /** メーカーコード. */
    @Column(name = "mdf_maker_code")
    private String mdfMakerCode;

    /** メーカー名. */
    @Column(name = "mdf_maker_name")
    private String mdfMakerName;

    /** 製品発注日. */
    @Column(name = "product_order_at")
    private Date productOrderAt;

    /** 生産工程区分. */
    @Column(name = "production_status")
    @Convert(converter = DelischeProductionStatusTypeConverter.class)
    private DelischeProductionStatusType productionStatus;

    /** 納期遅延件数. */
    @Column(name = "late_delivery_at_cnt")
    private Integer lateDeliveryAtCnt;

    /** 製品発注数. */
    private Integer quantity;

    /** 納品依頼数合計. */
    @Column(name = "delivery_lot_sum")
    private Integer deliveryLotSum;

    /** 入荷数合計. */
    @Column(name = "arrival_lot_sum")
    private Integer arrivalLotSum;

    /** 売上数. */
    @Column(name = "pos_sales_quantity")
    private Integer posSalesQuantity;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 原価. */
    @Column(name = "product_cost")
    private BigDecimal productCost;

    /** 製品完納区分. */
    @Column(name = "product_complete_order")
    private String productCompleteOrder;

    /** 子要素存在フラグ. */
    @Column(name = "child_exists")
    private boolean childExists;

    /** 発注承認ステータス. */
    @Column(name = "order_approve_status")
    @Convert(converter = OrderApprovalTypeConverter.class)
    private OrderApprovalType orderApproveStatus;

    /** 優良誤認承認区分（組成）. */
    @Column(name = "quality_composition_status")
    @Convert(converter = QualityApprovalTypeConverter.class)
    private QualityApprovalType qualityCompositionStatus;

    /** 優良誤認承認区分（国）. */
    @Column(name = "quality_coo_status")
    @Convert(converter = QualityApprovalTypeConverter.class)
    private QualityApprovalType qualityCooStatus;

    /** 優良誤認承認区分（有害物質）. */
    @Column(name = "quality_harmful_status")
    @Convert(converter = QualityApprovalTypeConverter.class)
    private QualityApprovalType qualityHarmfulStatus;
}
