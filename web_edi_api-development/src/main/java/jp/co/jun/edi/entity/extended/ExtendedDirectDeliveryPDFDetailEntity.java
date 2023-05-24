package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * 納品出荷ファイルPDFフォーマットの明細Entity.
 *
 * ※ 明細とフッタ(合計行)で利用
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedDirectDeliveryPDFDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** カラーコード. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 納品数量. */
    @Column(name = "delivery_lot")
    private Integer deliveryLot;

    /** 上代金額. */
    @Column(name = "retail_price_sub_total")
    private BigDecimal retailPriceSubTotal;

}
