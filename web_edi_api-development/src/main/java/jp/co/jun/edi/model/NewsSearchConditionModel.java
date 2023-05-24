package jp.co.jun.edi.model;

import java.io.Serializable;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * お知らせ情報検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsSearchConditionModel implements Serializable, SearchCondition {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 10;

    /** 1つの結果ページで返されるリストの最大数.デフォルトは10件. */
    @Min(value = MAX_RESULTS_MIN, groups = Default.class)
    @Max(value = MAX_RESULTS_MAX, groups = Default.class)
    private int maxResults = MAX_RESULTS_DEFAULT;

    /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーターは無視する. */
    private String pageToken;

    /** 取得対象の結果ページ.0から開始. */
    @Min(value = 0, groups = Default.class)
    @Max(value = Integer.MAX_VALUE, groups = Default.class)
    private int page = 0;
}
