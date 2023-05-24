package jp.co.jun.edi.entity.key;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * メーカー返品一覧情報の情報のKey.
 */
@Embeddable
@Data
public class MakerReturnCompositeKey implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 伝票番号. */
    @Column(name = "voucher_number")
    private String voucherNumber;

}
