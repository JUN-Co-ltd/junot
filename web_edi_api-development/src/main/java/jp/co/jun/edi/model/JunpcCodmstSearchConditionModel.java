package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 発注生産システムのコードマスタの検索用Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JunpcCodmstSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

    /** 検証用. */
    public interface Brand {
    };

    /** 検証用. */
    public interface Kind {
    };

    /** 検証用. */
    public interface Item {
    };

    /** 検証用. */
    public interface Code1 {
    };

    /** 検証用. */
    public interface SearchType {
    };

    /** 検証用. */
    public interface SearchText {
    };

    /** 検証用. */
    public interface StaffType {
    };

    /** 検証用. */
    public interface DivisionCode {
    };

    @NotEmpty(groups = { Brand.class })
    private String brand;
    @NotEmpty(groups = { Kind.class })
    private String kind;
    @NotEmpty(groups = { Item.class })
    private String item;
    @NotEmpty(groups = { Code1.class })
    private String code1;
    @NotEmpty(groups = { DivisionCode.class })
    private String divisionCode;

    private List<String> code1s;

    @NotEmpty(groups = { StaffType.class })
    private Integer staffType;
    @NotNull(groups = { SearchType.class })
    private String searchType;
    @NotEmpty(groups = { SearchText.class })
    private String searchText;

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
