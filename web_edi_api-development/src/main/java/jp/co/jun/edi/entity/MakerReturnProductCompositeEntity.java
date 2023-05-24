package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import jp.co.jun.edi.entity.key.MakerReturnProductCompositeKey;
import lombok.Data;

/**
 * メーカー返品商品情報のEntity.
 */
@Entity
@Data
public class MakerReturnProductCompositeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 複合主キー. */
    @EmbeddedId
    private MakerReturnProductCompositeKey key;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** ブランドコード. */
    @Column(name = "brand_code")
    private String brandCode;

    /** アイテムコード. */
    @Column(name = "item_code")
    private String itemCode;

    /** 発注番号. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 発注日. */
    @Column(name = "product_order_at")
    private Date productOrderAt;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 下代(発注の単価). */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    /** 最新の単価(品番情報のその他原価). */
    @Column(name = "other_cost")
    private BigDecimal otherCost;

    /** 在庫数. */
    @Column(name = "stock_lot")
    private Integer stockLot;
}
