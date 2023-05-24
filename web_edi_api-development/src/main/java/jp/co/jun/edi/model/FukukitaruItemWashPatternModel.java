package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * フクキタル洗濯ネーム付記用語情報のModel.
 *
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FukukitaruItemWashPatternModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** フクキタル洗濯ネーム付記用語情報ID. */
    private BigInteger id;

    /** 洗濯マークID. */
    private BigInteger washPatternId;

    /** カラーコード. */
    private String colorCode;

    /** 洗濯マークコード. */
    @Column(name = "wash_pattern_code")
    private String washPatternCode;

    /** 洗濯マーク名称. */
    @Column(name = "wash_pattern_name")
    private String washPatternName;
}
