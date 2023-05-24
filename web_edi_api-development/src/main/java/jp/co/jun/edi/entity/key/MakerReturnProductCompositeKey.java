package jp.co.jun.edi.entity.key;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * メーカー返品商品情報のKey.
 */
@Embeddable
@Data
public class MakerReturnProductCompositeKey implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** カラーコード. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    private String size;
}
