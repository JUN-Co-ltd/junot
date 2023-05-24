package jp.co.jun.edi.component.model;

import java.math.BigInteger;

import lombok.Data;

/**
 * メーカー返品のキーのModel.
 */
@Data
public class MakerReturnKeyModel {

    /** 伝票番号. */
    private final String voucherNumber;

    /** 発注ID. */
    private final BigInteger orderId;

    /**
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     */
    public MakerReturnKeyModel(final String voucherNumber, final BigInteger orderId) {
        this.voucherNumber = voucherNumber;
        this.orderId = orderId;
    }
}
