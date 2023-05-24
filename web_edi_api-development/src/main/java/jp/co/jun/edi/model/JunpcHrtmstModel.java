package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 配分率マスタのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JunpcHrtmstModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ブランド. */
    private String brandCode;

    /** アイテム. */
    private String itemCode;

    /** シーズン. */
    private String season;

    /** 配分率区分. */
    private String hrtkbn;

    /** 配分率名. */
    private String rtname;

    /** 配分率合計. */
    private BigDecimal totalHritu;

    /** 課別配分率. */
    private List<JunpcHrtmstDivisionModel> hrtmstDivisions;

}
