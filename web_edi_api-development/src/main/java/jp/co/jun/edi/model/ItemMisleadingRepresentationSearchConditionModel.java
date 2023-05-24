package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.EntireQualityApprovalType;
import lombok.Data;

/**
 * 優良誤認検査承認一覧情報検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemMisleadingRepresentationSearchConditionModel implements Serializable, SearchCondition {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

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

    /** ブランド. */
    private String brandCode;

    /** アイテム. */
    private String itemCode;

    /** 年度. */
    private Integer year;

    /** シーズンリスト. */
    private List<String> subSeasonCodeList;

    /** 品種. */
    private String partNoKind;

    /** 通番. */
    private String partNoSerialNo;

    /** 製品納期from. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date productCorrectionDeliveryAtFrom;

    /** 製品納期to. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date productCorrectionDeliveryAtTo;

    /** 優良誤認ステータス対象リスト. */
    private List<EntireQualityApprovalType> qualityStatusList;

    /** 検査承認日from. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date approvalAtFrom;

    /** 検査承認日to. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    private Date approvalAtTo;
}
