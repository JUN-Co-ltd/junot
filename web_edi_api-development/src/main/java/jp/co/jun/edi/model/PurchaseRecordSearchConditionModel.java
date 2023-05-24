//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 仕入実績一覧の検索条件のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseRecordSearchConditionModel implements Serializable, SearchCondition {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

    /** 対象ディスタ. */
    private String arrivalShop;

    /** 仕入区分. */
//    private PurchaseRecordType purchaseType;
    private Integer purchaseType;

    /** 計上日From. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date recordAtFrom;

    /** 計上日To. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date recordAtTo;

    /** 品番. */
    private String partNo;

    /** 会社コード. */
    private String comCode;

    /** 仕入先（メーカー）. */
    private String mdfMakerCode;

    /** 仕入先(コード). */
    private String sirCodes;

    /** 事業部コード. */
    private String divisionCode;

    /**費目.製品. */
    private boolean expenseProduct;

    /**費目.生地. */
    private boolean expenseMaterial;

    /**費目.附属. */
    private boolean expenseAttached;

    /**費目.加工. */
    private boolean expenseProcessing;

    /**費目.その他. */
    private boolean expenseOther;



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
}
//PRD_0133 #10181 add JFE end