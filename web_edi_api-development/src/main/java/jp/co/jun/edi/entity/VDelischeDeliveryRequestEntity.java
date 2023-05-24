package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import jp.co.jun.edi.entity.converter.DeliveryAtLateTypeConverter;
import jp.co.jun.edi.entity.converter.OrderApprovalTypeConverter;
import jp.co.jun.edi.entity.converter.QualityApprovalTypeConverter;
import jp.co.jun.edi.entity.key.VDelischeDeliveryRequestKey;
import jp.co.jun.edi.type.DeliveryAtLateType;
import jp.co.jun.edi.type.OrderApprovalType;
import jp.co.jun.edi.type.QualityApprovalType;
import lombok.Data;

/**
 * デリスケ納品依頼情報ViewのEntity.
 */
@Entity
@Data
public class VDelischeDeliveryRequestEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 複合主キー. */
    @EmbeddedId
    private VDelischeDeliveryRequestKey vDelischeDeliveryRequestKey;

    /** 月度. */
    @Column(name = "delivery_at_monthly")
    private Integer deliveryAtMonthly;

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

    /** メーカーコード. */
    @Column(name = "mdf_maker_code")
    private String mdfMakerCode;

    /** メーカー名. */
    @Column(name = "mdf_maker_name")
    private String mdfMakerName;

    /** 製品発注日. */
    @Column(name = "product_order_at")
    private Date productOrderAt;

    /** 製品納期. */
    @Column(name = "product_delivery_at")
    private Date productDeliveryAt;

    /** 納期遅延フラグ. */
    @Column(name = "late_delivery_at_flg")
    @Convert(converter = DeliveryAtLateTypeConverter.class)
    private DeliveryAtLateType lateDeliveryAtFlg;

    /** 発注数合計. */
    @Column(name = "product_order_lot_sum")
    private Integer productOrderLotSum;

    /** 納品依頼数合計. */
    @Column(name = "delivery_lot_sum")
    private Integer deliveryLotSum;

    /** 入荷数合計. */
    @Column(name = "arrival_lot_sum")
    private Integer arrivalLotSum;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 原価. */
    @Column(name = "product_cost")
    private BigDecimal productCost;

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
