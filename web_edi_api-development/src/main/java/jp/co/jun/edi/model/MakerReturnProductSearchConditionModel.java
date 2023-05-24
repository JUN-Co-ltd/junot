package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * メーカー返品商品情報検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MakerReturnProductSearchConditionModel implements Serializable, SearchCondition {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

    /** 発注ID. */
    private BigInteger orderId;

    /** ブランドコードリスト. */
    private List<String> brandCodes;

    /** アイテムコードリスト. */
    private List<String> itemCodes;

    /** サブシーズンコードリスト. */
    private List<String> subSeasonCodes;

    /** カラーコードリスト. */
    private List<String> colorCodes;

    /** サイズリスト. */
    private List<String> sizeList;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** 商品コードの品番. */
    private String partNoOfProductCode;

    /** 商品コードのカラーコード. */
    private String colorCodeOfProductCode;

    /** 商品コードのサイズ. */
    private String sizeOfProductCode;

    /** 仕入先コード. */
    private String supplierCode;

    /** 店舗コード. */
    private String shpcd;

    /** 上代from. */
    private BigDecimal retailPriceFrom;

    /** 上代to. */
    private BigDecimal retailPriceTo;

    /** 最新発注分のみ. */
    private boolean latestOrderOnly;

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
