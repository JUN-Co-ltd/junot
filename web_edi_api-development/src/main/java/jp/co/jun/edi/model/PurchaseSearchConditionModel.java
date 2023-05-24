package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.OnOffType;
import lombok.Data;

/**
 * 仕入一覧の検索条件のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseSearchConditionModel implements Serializable, SearchCondition {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

    /** 対象ディスタ. */
    private String arrivalShop;

    /** 納品日From. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date correctionAtFrom;

    /** 納品日To. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date correctionAtTo;

    /** 仕入先（メーカー）. */
    private String mdfMakerCode;

    /** 納品No From. */
    private String deliveryNumberFrom;

    /** 納品No To. */
    private String deliveryNumberTo;

    /** 仕入. */
    private OnOffType arrivalFlg;

    /** 送信. */
    private OnOffType lgSendFlg;

    /** 入荷日from. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date arrivalAtFrom;

    /** 入荷日to. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date arrivalAtTo;

    // PRD_0021 add SIT start
    /** 品番. */
    private String partNo;

    /** ブランドコード. */
    private String brandCode;
    // PRD_0021 add SIT end

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
