package jp.co.jun.edi.model.maint;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.AuthorityType;
import lombok.Data;

/**
 * マスタメンテナンス用のユーザ検索結果のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintUserSearchResultModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** アカウント名. */
    private String accountName;

    /** 有効/無効. */
    private boolean enabled;

    /** 権限. */
    private List<AuthorityType> authorities;

    /** 所属会社. */
    private String company;

    /** 氏名. */
    private String name;

    /** メーカー名称. */
    private String makerName;

    /** メールアドレス（カンマ区切り）. */
    private String mailAddress;
}
