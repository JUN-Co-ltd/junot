package jp.co.jun.edi.model;

import java.io.Serializable;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 発注生産システムの配分率マスタを取得するAPIの検索条件Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JunpcHrtmstSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

    /** ブランド. */
    private String brandCode;

    /** アイテム. */
    private String itemCode;

    /** シーズン. */
    private String season;

    /** 配分率区分. */
    private String hrtkbn;

    /** 1つの結果ページで返されるリストの最大数.デフォルトは100件. */
    @Min(MAX_RESULTS_MIN)
    @Max(MAX_RESULTS_MAX)
    private int maxResults = MAX_RESULTS_DEFAULT;

    /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
    private String pageToken;

    /** 取得対象の結果ページ.0から開始. */
    @Min(0)
    @Max(Integer.MAX_VALUE)
    private int page = 0;
}
