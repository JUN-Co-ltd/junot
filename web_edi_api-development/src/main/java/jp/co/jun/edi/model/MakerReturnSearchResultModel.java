package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.LgSendType;
import lombok.Data;

/**
 * メーカー返品一覧検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MakerReturnSearchResultModel implements Serializable, SearchCondition {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

    /** 店舗コード(ディスタ選択). */
    private String shpcd;

    /** 伝票入力日from. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date voucherNumberInputAtFrom;

    /** 伝票入力日to. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date voucherNumberInputAtTo;

    /** メーカー . */
    private String supplierCode;

    /** 伝票日付from . */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date voucherNumberAtFrom;

    /** 伝票日付to . */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date voucherNumberAtTo;

    /** 担当者 . */
    private String mdfStaffCode;

    /** 伝票番号from. */
    private String voucherNumberFrom;

    /** 伝票番号to. */
    private String voucherNumberTo;

    /** 物流コード. */
    private String logisticsCode;

    /** 伝票番号. */
    private String voucherNumber;

    /** 状態(LG送信区分)  . */
    private LgSendType lgSendType;

    /** 返却日(伝票日付). */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date returnAt;

    /** 仕入先名称 .*/
    private String supplierName;

    /** 数量. */
    private String returnLot;

    /** 単価. */
    private String unitPrice;

    /** 発注番号. */
    private String orderNumber;

    /** 登録日(伝票入力日). */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date createdAt;
    /** 1つの結果ページで返されるリストの最大数.デフォルトは100件. */
    @Min(value = MAX_RESULTS_MIN, groups = Default.class)
    @Max(value = MAX_RESULTS_MAX, groups = Default.class)
    private int maxResults = MAX_RESULTS_DEFAULT;

    /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
    private String pageToken;

    /** 取得対象の結果ページ.0から開始. */
    @Min(value = 0, groups = Default.class)
    @Max(value = Integer.MAX_VALUE, groups = Default.class)
    private int page = 0;
}
