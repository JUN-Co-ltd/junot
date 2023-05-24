package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.OrderByType;
import lombok.Data;

/**
 * 発注情報検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

    /** 発注ID. */
    private BigInteger id;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 発注No(複数検索用). */
    private String orderNumberText;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 品番. */
    private String partNo;

    /** 製品名. */
    private String productName;

    /** ブランドコード. */
    private String brandCode;

    /** アイテムコード. */
    private String itemCode;

    /** メーカー. */
    private String maker;

    /** サブシーズンコード. */
    private String subSeasonCode;

    /** 年度. */
    private Integer year;

    /** 製品納期年from. */
    private Integer productDeliveryAtYearFrom;

    /** 製品納期年to. */
    private Integer productDeliveryAtYearTo;

    /** 製品納期月from. */
    private Integer productDeliveryAtMonthlyFrom;

    /** 製品納期月to. */
    private Integer productDeliveryAtMonthlyTo;

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
