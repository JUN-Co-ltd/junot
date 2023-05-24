package jp.co.jun.edi.model;

import java.io.Serializable;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.ShopKindType;
import lombok.Data;

/**
 * 発注生産システムの店舗マスタを取得するAPIのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JunpcTnpmstSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = Integer.MAX_VALUE;

    /** 事業部コード. */
    private String divisionCode;

    /** 店舗コード. */
    private String shpcd;

    /** 店舗コード前方一致. */
    private String shpcdAhead;

    /** 店舗名. */
    private String name;

    /** 電話番号. */
    private String telban;

    /** 店舗区分. */
    private ShopKindType shopkind;

    /** ソート. */
    private SortModel sort = new SortModel();

    /** 1つの結果ページで返されるリストの最大数. */
    @Min(MAX_RESULTS_MIN)
    @Max(MAX_RESULTS_MAX)
    private int maxResults = Integer.MAX_VALUE;

    /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
    private String pageToken;

    /** 取得対象の結果ページ.0から開始. */
    @Min(0)
    @Max(Integer.MAX_VALUE)
    private int page = 0;
}
