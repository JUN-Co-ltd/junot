package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.SendMailStatusType;
import lombok.Data;

/**
 * 返品伝票管理のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReturnVoucherModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 伝票番号. */
    private String voucherNumber;

    /** 発注ID. */
    private BigInteger orderId;

    /** 状態:0：処理前、1：処理中、2：処理済み. */
    private SendMailStatusType status;
}
