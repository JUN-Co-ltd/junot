package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.DelischeProductionStatusTypeConverter;
import jp.co.jun.edi.entity.key.VDelischeCsvKey;
import jp.co.jun.edi.type.DelischeProductionStatusType;
import lombok.Data;

/**
 * デリスケCSVのEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class VDelischeCsvEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 複合主キー. */
    @EmbeddedId
    private VDelischeCsvKey vDelischeCsvKey;

    /** 製品納期. */
    @Column(name = "product_delivery_at")
    private Date productDeliveryAt;

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

    /** サイズ表示順. */
    private String jun;

    /** シーズン. */
    private String season;

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

    /** 製品発注数. */
    private Integer quantity;

    /** 発注数(納品依頼)合計. */
    @Column(name = "product_order_lot_sum")
    private Integer productOrderLotSum;

    /** 数量(発注数). */
    @Column(name = "product_order_lot")
    private Integer productOrderLot;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 原価. */
    @Column(name = "product_cost")
    private BigDecimal productCost;

    /** 納品月度(納品依頼・SKU). */
    @Column(name = "delivery_at_monthly")
    private Integer deliveryAtMonthly;

    /** 納品月度(発注). */
    @Column(name = "product_delivery_at_monthly")
    private Integer productDeliveryAtMonthly;

    /** 納期遅延件数. */
    @Column(name = "late_delivery_at_cnt")
    private Integer lateDeliveryAtCnt;

    /** 納品依頼数(発注)合計. */
    @Column(name = "delivery_lot_order_sum")
    private Integer deliveryLotOrderSum;

    /** 納品依頼数合計. */
    @Column(name = "delivery_lot_sum")
    private Integer deliveryLotSum;

    /** 納品依頼数. */
    @Column(name = "delivery_lot")
    private Integer deliveryLot;

    /** 入荷数(発注)合計. */
    @Column(name = "arrival_lot_order_sum")
    private Integer arrivalLotOrderSum;

    /** 入荷数合計. */
    @Column(name = "arrival_lot_sum")
    private Integer arrivalLotSum;

    /** 入荷数. */
    @Column(name = "arrival_lot")
    private Integer arrivalLot;

    /** 売上数. */
    @Column(name = "pos_sales_quantity")
    private Integer posSalesQuantity;
}
