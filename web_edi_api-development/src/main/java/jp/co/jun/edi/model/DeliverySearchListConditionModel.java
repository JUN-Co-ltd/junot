package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.CarryType;
import jp.co.jun.edi.type.DeliveryListAllocationStatusType;
import lombok.Data;

/**
 * 配分一覧検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliverySearchListConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

    /** 品番. */
    private String partNo;

    /** ブランドコード. */
    private String brandCode;

    /** 事業部. */
    private String departmentCode;

    /** 内訳(キャリー区分). */
    private CarryType carryType;

    /**
     * 完納フラグ
     * false：未完(0)、true:完納(1)
     * . */
    private Boolean orderCompleteFlg;

    /** 仕入フラグ. */
    private Boolean purchasesFlg;

    /** 承認フラグ. */
    private Boolean approvaldFlg;

    /** 出荷フラグ. */
    private Boolean shipmentFlg;

    // PRD_0037 mod SIT start
    ///** 要再配分フラグ. */
    //private Boolean reAllocationFlg;
    /** 配分状態. */
    private DeliveryListAllocationStatusType allocationStatusType;
    // PRD_0037 moe SIT end

    /** 仕入先(生産メーカー). */
    private String mdfMakerCode;

    /** 納品日(修正納期)from. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryAtFrom;

    /** 納品日(修正納期)to. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryAtTo;

    /** 発注番号from. */
    private BigInteger orderNumberFrom;

    /** 発注番号to. */
    private BigInteger orderNumberTo;

    /** 納品番号from. */
    private String deliveryNumberFrom;

    /** 納品番号fto. */
    private String deliveryNumberTo;

    /** 納品依頼番号from. */
    private String deliveryRequestNumberFrom;

    /** 納品依頼番号to. */
    private String deliveryRequestNumberTo;

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
