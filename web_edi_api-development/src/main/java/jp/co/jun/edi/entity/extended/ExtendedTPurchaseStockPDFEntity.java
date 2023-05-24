package jp.co.jun.edi.entity.extended;

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
//PRD_0134 #10654 add JEF start
/**
 * ExtendedTPurchaseStockPDFEntity.
 * 仕入明細PDFデータ用
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTPurchaseStockPDFEntity {
    /** 発注ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 入荷先コード. */
    @Column(name = "stock_code")
    private String stockCode;

    /** 入荷先名. */
    @Column(name = "stock_name")
    private String stockName;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 依頼No. */
    @Column(name = "request_number")
    private BigInteger requestNumber;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 全済区分. */
    @Column(name = "all_completion_type")
    private int allCompletionType;

    /** 部門コード. */
    @Column(name = "division_code")
    private String divisionCode;

    /** ブランド名. */
    @Column(name = "brand_name")
    private String brandName;

    /** アイテム名. */
    @Column(name = "item_name")
    private String itemName;

    /** 数量. */
    private BigDecimal quantity;

    /** 仕入単価. */
    private BigDecimal purchaseUnitPrice;

    /** 発注単価. */
    private BigDecimal unitPrice;

    /** B級品単価. */
    private BigDecimal nonConformingProductUnitPrice;
	//PRD_0134 #10654 add JEF end
}
