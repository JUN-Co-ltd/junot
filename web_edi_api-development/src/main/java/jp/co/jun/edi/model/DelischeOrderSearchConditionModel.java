package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * デリスケ発注情報検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelischeOrderSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

    /** 事業部コード. */
    private String divisionCode;

    /** 事業部コードから取得したブランドコードリスト. */
    private List<String> brandCodeListFromDivision;

    /** ブランドコード. */
    private String brandCode;

    /** アイテムコード. */
    private String itemCode;

    /** 品番. */
    private String partNo;

    /** シーズン. */
    private List<String> season;

    /** メーカー. */
    private String mdfMaker;

    /** 製造担当. */
    private String mdfStaff;

    /** 発注納期from. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date productDeliveryAtFrom;

    /** 発注納期to. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date productDeliveryAtTo;

    /** 納品日from. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date deliveryAtFrom;

    /** 納品日to. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date deliveryAtTo;

    /** 年度・月度から変換した生産納期from. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date productDeliveryAtFromByMonthly;

    /** 年度・月度から変換した生産納期to. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date productDeliveryAtToByMonthly;

    /** 年度・納品週から変換した納期from. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date deliveryAtFromByMdweek;

    /** 年度・納品週から変換した納期to. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date deliveryAtToByMdweek;

    // PRD_0146 #10776 add JFE start
    /** 費目. */
    private String expenseItem;
    // PRD_0146 #10776 add JFE end

    /** 納品遅れ. */
    private boolean deliveryAtLateFlg;

    /** 完納は対象外. */
    private boolean excludeCompleteOrder;

    /** 発注残あり. */
    private boolean existsOrderRemaining;

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
