package jp.co.jun.edi.entity;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店別在庫情報のEntity.
 */
@Entity
@Table(name = "t_shop_stock")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TShopStockEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 店舗コード. */
    @Column(name = "shop_code")
    private String shopCode;

    /** 商品コード. */
    @Column(name = "product_code")
    private String productCode;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** カラーコード. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    private String size;

    /** 在庫数. */
    @Column(name = "stock_lot")
    private Integer stockLot;
}
