package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.OccupationType;
import lombok.Data;

/**
 * セッション情報.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ユーザID. */
    private final BigInteger userId;

    /** アカウント名. */
    private final String accountName;

    /** 所属会社. */
    private final String company;

    /** 所属. true : 社内  false : 取引先 */
    private final boolean affiliation;

    /** 権限. */
    private final List<String> authorities;

    /** 発注承認可ブランド. */
    private final List<String> orderApprovalAuthorityBlands;

    /** 職種. */
    private final OccupationType occupationType;

    /**
     * @param user ユーザエンティティ
     */
    public SessionModel(final CustomLoginUser user) {
        Assert.notNull(user, "user must not be null");

        this.userId = user.getUserId();
        this.accountName = user.getAccountName();
        this.company = user.getCompany();
        this.authorities = user.getAuthorities().stream()
                .map((GrantedAuthority gAuthority) -> {
                    return gAuthority.getAuthority();
                })
                .collect(Collectors.toList());
        this.affiliation = user.isAffiliation();
        this.orderApprovalAuthorityBlands = user.getOrderApprovalAuthorityBlands();
        this.occupationType = user.getOccupationType();
    }
}
