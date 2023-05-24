package jp.co.jun.edi.model.maint;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.model.SearchCondition;
import jp.co.jun.edi.type.AuthorityType;
import jp.co.jun.edi.type.SearchMethodType;
import lombok.Data;

/**
 * マスタメンテナンス用のユーザ情報検索のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintUserSearchConditionModel implements Serializable, SearchCondition {
    private static final long serialVersionUID = 1L;

    private static final int KEYWORD_CONDITIONS_LIMIT_SIZE = 10;

    private static final int AUTHORITY_CONDITIONS_LIMIT_SIZE = 4;

    private static final int MAX_RESULTS_MIN = 1;
    private static final int MAX_RESULTS_MAX = 1000;
    private static final int MAX_RESULTS_DEFAULT = 100;

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

    /**
     * システム管理.
     * デフォルトはfalse.
     */
    private boolean systemManaged = false;

    /** アカウント名（ログインID）. */
    @Size(max = KEYWORD_CONDITIONS_LIMIT_SIZE, groups = Default.class)
    private List<String> accountNames;

    /** 所属会社（会社コード）. */
    @Size(max = KEYWORD_CONDITIONS_LIMIT_SIZE, groups = Default.class)
    private List<String> companies;

    /** メーカーコード. */
    @Size(max = KEYWORD_CONDITIONS_LIMIT_SIZE, groups = Default.class)
    private List<String> makerCodes;

    /** メーカー名称. */
    @Size(max = KEYWORD_CONDITIONS_LIMIT_SIZE, groups = Default.class)
    private List<String> makerNames;

    /** 氏名. */
    @Size(max = KEYWORD_CONDITIONS_LIMIT_SIZE, groups = Default.class)
    private List<String> names;

    /** メールアドレス. */
    @Size(max = KEYWORD_CONDITIONS_LIMIT_SIZE, groups = Default.class)
    private List<String> mailAddresses;

    /** 権限. */
    @Size(max = AUTHORITY_CONDITIONS_LIMIT_SIZE, groups = Default.class)
    private List<AuthorityType> authorities;

    /** 有効/無効. */
    @Size(max = 2, groups = Default.class)
    private List<Boolean> enabledList;

    /**
     * 検索方法.
     * デフォルトはすべてOR検索。
     * 指定可能な検索方法。
     * - ALL_AND_FULL : すべてAND検索 完全一致
     * - ALL_OR_LIKE : すべてOR検索 部分一致（システム管理のみAND検索）
     */
    private SearchMethodType searchMethod = SearchMethodType.ALL_OR_LIKE;
}
