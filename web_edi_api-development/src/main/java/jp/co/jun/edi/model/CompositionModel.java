package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.constants.SizeConstants;
import lombok.Data;

/**
 * 組成情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompositionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_CODE_PREFIX = "{jp.co.jun.edi.model.CompositionModel";

    /** ID. */
    private BigInteger id;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 品番. */
    private String partNo;

    /** 連番. */
    private Integer serialNumber;

    /** 色. */
    private String colorCode;

    /** 色名称. */
    private String colorName;

    /** パーツ. */
    private String partsCode;

    /** パーツ名称. */
    private String partsName;

    /** 組成. */
    private String compositionCode;

    /** 組成名称. */
    private String compositionName;

    /** 混率. */
    @Min(value = SizeConstants.PERCENT_MIN, message = MESSAGE_CODE_PREFIX + ".percent.Min}", groups = { Default.class })
    @Max(value = SizeConstants.PERCENT_MAX, message = MESSAGE_CODE_PREFIX + ".percent.Max}", groups = { Default.class })
    private Integer percent;
}
