package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 課別配分率マスタのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JunpcHrtmstDivisionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 店舗:配分課. */
    private String shpcd;

    /** 配分率. */
    private BigDecimal hritu;

}
