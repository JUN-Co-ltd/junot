package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 仕入配分課FormのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseDivisionFormModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 仕入ID. */
    private BigInteger id;
    /** 入荷数. */
    private Integer arrivalCount;
    /** 入荷確定数. */
    private Integer fixArrivalCount;
    /** 配分課. */
    private String divisionCode;
}
