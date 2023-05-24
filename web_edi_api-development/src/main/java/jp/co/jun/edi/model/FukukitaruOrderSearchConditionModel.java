package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * フクキタル発注情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FukukitaruOrderSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 発注ID. */
    private BigInteger orderId;

    /** 品番ID. */
    private BigInteger partNoId;


}
