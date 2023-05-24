package jp.co.jun.edi.model.maint;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.constants.RegexpConstants;
import jp.co.jun.edi.type.AuthorityType;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import lombok.Data;

/**
 * マスタメンテナンス用のユーザ情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintUserModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int PASSWORD_SIZE_MIN = 4;
    private static final int PASSWORD_SIZE_MAX = 20;
    private static final int NAME_SIZE_MAX = 100;
    private static final int MAIL_ADDRESS_SIZE_MAX = 600;

    /** ID. */
    private BigInteger id;

    /** アカウント名. */
    @NotNull(groups = Default.class)
    @Pattern(regexp = RegexpConstants.USER_ACCOUNT_NAME, groups = Default.class)
    private String accountName;

    /** パスワード. */
    @NotNull(groups = CreateValidationGroup.class)
    @Size(min = PASSWORD_SIZE_MIN, groups = Default.class)
    @Size(max = PASSWORD_SIZE_MAX, groups = Default.class)
    private String password;

    /** 有効/無効. */
    @NotNull(groups = Default.class)
    private boolean enabled;

    /** 権限. */
    @NotNull(groups = Default.class)
    private List<AuthorityType> authorities;

    /** 所属会社. */
    @Pattern(regexp = RegexpConstants.USER_COMPANY, groups = Default.class)
    private String company;

    /** 氏名. */
    @Size(max = NAME_SIZE_MAX, groups = Default.class)
    private String name;

    /** メールアドレス（カンマ区切り）. */
    @Size(max = MAIL_ADDRESS_SIZE_MAX, groups = Default.class)
    @Pattern(regexp = RegexpConstants.EMAIL_COMMA_DELIMITED, groups = Default.class)
    private String mailAddress;

    /** メーカー名称. 画面表示のみ. */
    private String makerName;
}
