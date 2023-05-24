// CHECKSTYLE:OFF
package test.utils;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.AuthorityType;

public class LoginUserUtils {
    /**
     * ログインユーザ情報を生成
     * @param user ユーザ
     * @return ログインユーザ情報
     */
    public static CustomLoginUser generateLoginUser(final MUserEntity user) {
        // GrantedAuthorityに変換
        final List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getAuthority());

        // 社内権限か確認
        final boolean affiliation = authorities.stream().anyMatch(s -> AuthorityType.ROLE_JUN == AuthorityType.convertToType(s.getAuthority()));

        return new CustomLoginUser(user, authorities, affiliation, Collections.emptyList(), null);
    }
}
//CHECKSTYLE:ON
