package jp.co.jun.edi.model;

import java.io.Serializable;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.OrderByType;
import lombok.Data;

/**
 * 品番検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

    /** 品番. */
    private String partNo;

    /** 製品名. */
    private String productName;

    /** ブランドコード. */
    private String brandCode;

    /** アイテムコード. */
    private String itemCode;

    /** サブシーズンコード. */
    private String subSeasonCode;

    /** 年度. */
    private Integer year;

    /** 企画担当者名. */
    private String plannerName;

    /** 製造担当者名. */
    private String mdfStaffName;

    /** パターンナー名. */
    private String patanerName;

    /** 生産メーカー担当者名. */
    private String mdfMakerStaffName;

    /** ID昇順/降順指定.
     * 0: idで昇順.
     * 1: idで降順.
    */
    @Min(0)
    @Max(1)
    private int idOrderBy = OrderByType.ASC.getValue();

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
