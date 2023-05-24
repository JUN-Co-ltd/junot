package jp.co.jun.edi.security;

import java.math.BigInteger;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.type.OccupationType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * ログインユーザの情報.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class CustomLoginUser extends User {
    private static final long serialVersionUID = 1L;

    /** ユーザID. */
    private BigInteger userId;

    /** アカウント名. */
    private String accountName;

    /** 所属会社. */
    private String company;

    /** 所属. true : 社内  false : 取引先 */
    private boolean affiliation;

    /** 発注承認可ブランド. */
    private final List<String> orderApprovalAuthorityBlands;

    /** 職種. */
    private final OccupationType occupationType;

    /**
     * @param user ユーザエンティティ
     */
    /**
     * @param user ユーザエンティティ
     * @param authorities 権限
     * @param affiliation 所属. true : 社内  false : 取引先
     * @param orderApprovalAuthorityBlands 発注承認可ブランド
     * @param occupationType 職種
     */
    public CustomLoginUser(
            final MUserEntity user,
            final List<GrantedAuthority> authorities,
            final boolean affiliation,
            final List<String> orderApprovalAuthorityBlands,
            final OccupationType occupationType) {
        super(user.getAccountName(), user.getPassword(), authorities);
        this.userId = user.getId();
        this.accountName = user.getAccountName();
        this.company = user.getCompany();
        this.userId = user.getId();
        this.affiliation = affiliation;
        this.orderApprovalAuthorityBlands = orderApprovalAuthorityBlands;
        this.occupationType = occupationType;
    }
}
