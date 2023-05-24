package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * メーカー返品指示(LG送信)用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MakerReturnInstructionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 発注ID. */
    private BigInteger orderId;

    /** 伝票番号. */
    private String voucherNumber;
}
