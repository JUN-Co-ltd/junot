package jp.co.jun.edi.security;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.MCodmstComponent;
import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.repository.MUserRepository;
import jp.co.jun.edi.type.AuthorityType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * ユーザマスタを検索する.
 */
@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private MCodmstComponent mCodmstComponent;

    @Autowired
    private MUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(final String loginId) {
        // パラメータのログインIDをアカウント名と、会社に分離する。
        final String[] str = StringUtils.split(loginId, "\n");

        if (str.length != 2) {
            throw new UsernameNotFoundException("user not found");
        }

        final String company = str[0];
        final String accountName = str[1];

        if (StringUtils.isEmpty(company)) {
            throw new UsernameNotFoundException("user not found");
        }

        if (StringUtils.isEmpty(accountName)) {
            throw new UsernameNotFoundException("user not found");
        }

        // 会社コードで検索。
        Integer dataCnt = userRepository.countByCompany(company);
        if (dataCnt == 0) {
            // 会社コードが登録されていないエラーをスロー
            throw new UsernameNotFoundException(MessageCodeType.CODE_L_001.getValue());
        }

        // 会社コードとアカウント名で検索
        final Optional<MUserEntity> dataByCompanyAndAccountName = userRepository.findByAccountNameAndCompany(accountName, company);
        if (!dataByCompanyAndAccountName.isPresent()) {
            // アカウント名に紐づく会社コードがないエラーをスロー
            throw new UsernameNotFoundException(MessageCodeType.CODE_L_002.getValue());
        }

        final MUserEntity mUserEntity = dataByCompanyAndAccountName.get();

        // QA権限取得
        final String authority = mUserEntity.getAuthority();
        if (isQaAuth(accountName)) {
            mUserEntity.setAuthority(authority + "," + AuthorityType.ROLE_QA.getValue());
        }

        // GrantedAuthorityに変換
        final List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(mUserEntity.getAuthority());

        // 利用ユーザ(ROLE_USER)であるか確認。
        final boolean isNotRoleUser = authorities.stream().noneMatch(s -> AuthorityType.ROLE_USER == AuthorityType.convertToType(s.getAuthority()));
        if (isNotRoleUser) {
            // 利用権限がない場合はログイン不可のため、エラーをスロー
            throw new AccessDeniedException(MessageCodeType.CODE_L_003.getValue());
        }

        // 社内権限か確認
        final boolean affiliation = authorities.stream().anyMatch(s -> AuthorityType.ROLE_JUN == AuthorityType.convertToType(s.getAuthority()));

        final CustomLoginUser loginUser;

        // 社内権限の時は社員マスタの発注承認可ブランドと職種を取得する。
        if (affiliation) {
            loginUser = new CustomLoginUser(mUserEntity, authorities, affiliation,
                    mCodmstComponent.getOrderApprovalAuthorityBlands(mUserEntity.getAccountName()),
                    mCodmstComponent.getOccupationType(mUserEntity.getAccountName()));
        } else {
            loginUser = new CustomLoginUser(mUserEntity, authorities, affiliation, Collections.emptyList(), null);
        }

        return loginUser;
    }

    /**
     * QA権限か判定する.
     * @param accountName アカウント名
     * @return true:QA権限
     */
    private boolean isQaAuth(final String accountName) {
        return mCodmstComponent.isQaAuth(accountName);
    }

}
