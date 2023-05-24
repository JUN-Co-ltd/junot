package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.LgSendType;
import lombok.Data;

/**
 * 仕入配分課のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseDivisionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 仕入ID. */
    private BigInteger id;

    /** 入荷数. */
    @NotNull(groups = { Default.class })
    @Min(value = 0, groups = Default.class)
    private Integer arrivalCount;

    /** 入荷確定数. */
    private Integer fixArrivalCount;

    /** 配分課. */
    private String divisionCode;

    /** LG送信区分. */
    private LgSendType lgSendType;
}
