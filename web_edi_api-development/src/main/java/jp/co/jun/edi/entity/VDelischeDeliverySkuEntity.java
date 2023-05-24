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

import jp.co.jun.edi.entity.converter.DeliveryAtLateTypeConverter;
import jp.co.jun.edi.entity.converter.OrderApprovalTypeConverter;
import jp.co.jun.edi.entity.converter.QualityApprovalTypeConverter;
import jp.co.jun.edi.type.DeliveryAtLateType;
import jp.co.jun.edi.type.OrderApprovalType;
import jp.co.jun.edi.type.QualityApprovalType;
import lombok.Data;

/**
 * デリスケ納品SKU情報ViewのEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class VDelischeDeliverySkuEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID(納品SKUID). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 納品ID. */
    @Column(name = "delivery_id")
    private BigInteger deliveryId;

    /** 納品明細ID. */
    @Column(name = "delivery_detail_id")
    private BigInteger deliveryDetailId;

    /** 納期. */
    @Column(name = "delivery_at")
    private Date deliveryAt;

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

    /** カラー. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    private String size;

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

    /** 数量(発注数). */
    @Column(name = "product_order_lot")
    private Integer productOrderLot;

    /** 納品依頼数. */
    @Column(name = "delivery_lot")
    private Integer deliveryLot;

    /** 入荷数. */
    @Column(name = "arrival_lot")
    private Integer arrivalLot;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 原価. */
    @Column(name = "product_cost")
    private BigDecimal productCost;

    /** サイズ表示順. */
    private String jun;

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
