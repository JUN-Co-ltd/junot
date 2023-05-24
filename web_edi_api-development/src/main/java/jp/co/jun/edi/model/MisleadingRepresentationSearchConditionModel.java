package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 優良誤認承認情報検索のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MisleadingRepresentationSearchConditionModel implements Serializable, SearchCondition {
    private static final long serialVersionUID = 1L;

    private static final int SEASON_LIMIT_SIZE = 3;
    private static final int INSPECTION_STATUS_LIMIT_SIZE = 3;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

    /** 1つの結果ページで返されるリストの最大数.デフォルトは100件. */
    @Min(value = MAX_RESULTS_MIN, groups = Default.class)
    @Max(value = MAX_RESULTS_MAX, groups = Default.class)
    private int maxResults = MAX_RESULTS_DEFAULT;

    /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーターは無視する. */
    private String pageToken;

    /** 取得対象の結果ページ.0から開始. */
    @Min(value = 0, groups = Default.class)
    @Max(value = Integer.MAX_VALUE, groups = Default.class)
    private int page = 0;

    /** ブランドコード. */
    private String brandCode;

    /** アイテムコード. */
    private String itemCode;

    /** 年度. */
    private Integer year;

    /** シーズン(複数). */
    @Size(max = SEASON_LIMIT_SIZE, groups = Default.class)
    private List<String> seasonCodeList;

    /** 品番. */
    private String partNo;

    /** 製品納期From. */
    private Date productDeliveryAtFrom;

    /** 製品納期To. */
    private Date productDeliveryAtTo;

    /** 検査状態(複数). */
    @Size(max = INSPECTION_STATUS_LIMIT_SIZE, groups = Default.class)
    private List<String> inspectionStatusList;

    /** 検査承認日From. */
    private Date inspectionApprovalAtFrom;

    /** 検査承認日To. */
    private Date inspectionApprovalAtTo;

}
