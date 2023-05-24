package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 閾値マスタのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThresholdModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** ブランドコード. */
    private String brandCode;

    /** アイテムコード. */
    private String itemCode;

    /** 閾値. */
    private BigDecimal threshold;
}
