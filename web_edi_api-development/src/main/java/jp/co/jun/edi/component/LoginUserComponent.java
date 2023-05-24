package jp.co.jun.edi.component;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * ログインユーザ関連のコンポーネント.
 */
@Component
public class LoginUserComponent extends GenericComponent {
    /** 連携入力者の初期値.社外などメーカーが更新した場合、こちらの値が設定される. */
    private static final String DEFAULT_JUNPC_TANTO = "000000";

    /**
     * 社員の場合、アカウント名を返却する.
     * 社外の場合、"000000"を返却する.
     * @param loginUser ログインユーザ情報
     * @return String
     */
    public String getAccountNameWithAffiliation(final CustomLoginUser loginUser) {

        if (loginUser.isAffiliation()) {
            return loginUser.getAccountName();
        }
        return DEFAULT_JUNPC_TANTO;
    }

    /**
     * メーカーの場合（社員でない場合）、IDを返却する.
     * @param loginUser ログインユーザ情報
     * @return String
     */
    public BigInteger getUserIdWithoutAffiliation(final CustomLoginUser loginUser) {

        if (!loginUser.isAffiliation()) {
            return loginUser.getUserId();
        }
        return null;
    }

    /**
     * ログイン中のユーザ情報取得.
     * @return ユーザ情報
     * @throws UsernameNotFoundException ログインユーザが取得出来な時に例外を発生
     */
    public CustomLoginUser getLoginUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("user not found");
        }

        return (CustomLoginUser) authentication.getPrincipal();

    }

    /**
     * メーカー権限チェック.
     *
     * @param user ログインユーザ権限
     * @param supplierCode メーカーコード
     * @return true;参照可 false:参照不可
     */
    public boolean isSupplierAuthority(final CustomLoginUser user, final String supplierCode) {
        if (user.isAffiliation()) {
            return true;
        }

        return StringUtils.equals(user.getCompany(), supplierCode);
    }

    /**
     * メーカー権限チェック.
     *
     * @param user ログインユーザ権限
     * @param supplierCode メーカーコード
     * @throws ResourceNotFoundException アクセス不可能なメーカーの場合、例外を発生
     */
    public void validateSupplierAuthority(final CustomLoginUser user, final String supplierCode) {
        if (!isSupplierAuthority(user, supplierCode)) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_005));
        }
    }

    /**
     * メーカー権限の場合、メーカーコードを取得する. 社内権限の場合、NULLを返却する.
     *
     * @param user ログインユーザ権限
     * @return メーカーコード
     */
    public String getSupplierCode(final CustomLoginUser user) {
        if (user.isAffiliation()) {
            return null;
        }

        return user.getCompany();
    }
}
