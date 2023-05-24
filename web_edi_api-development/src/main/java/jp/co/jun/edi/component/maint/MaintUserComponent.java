package jp.co.jun.edi.component.maint;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.GenericComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.entity.master.UserEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.maint.MaintUserModel;
import jp.co.jun.edi.model.maint.MaintUserSearchResultModel;
import jp.co.jun.edi.repository.MSirmstRepository;
import jp.co.jun.edi.repository.MUserRepository;
import jp.co.jun.edi.type.AuthorityType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * マスタメンテナンス用のユーザ情報関連のコンポーネント.
 */
@Component
public class MaintUserComponent extends GenericComponent {
    private static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private static final String AUTHORITY_SEPARATOR = ",";

    @Autowired
    private MUserRepository tUserRepository;

    @Autowired
    private MSirmstRepository mSirmstRepository;

    @Autowired
    private PropertyComponent propertyComponent;

    /**
     * ユーザ情報を取得する.
     *
     * @param userId ユーザID
     * @return {@link MUserEntity} instance
     * @throws ResourceNotFoundException ユーザが存在しない場合
     */
    public MUserEntity getMUser(final BigInteger userId) {
        return tUserRepository.findByIdAndSystemManagedFalseDeletedAtIsNull(userId).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
    }

    /**
     * ユーザ情報を保存する.
     *
     * @param entity {@link MUserEntity} instance
     * @return {@link MUserEntity} instance
     * @throws BusinessException CODE_005 : 会社コードとアカウント名が重複するユーザが存在する場合
     */
    public MUserEntity save(final MUserEntity entity) {
        // アカウント名と会社コードが重複するユーザがいるか検証.
        assertDuplicateUser(entity.getId(), entity.getAccountName(), entity.getCompany());

        return tUserRepository.save(entity);
    }

    /**
     * アカウント名と会社コードが重複するユーザがいるか検証する.
     *
     * @param userId ユーザID
     * @param accountName アカウント名
     * @param company 会社コード
     * @throws BusinessException CODE_005 : 会社コードとアカウント名が重複するユーザが存在する場合
     */
    public void assertDuplicateUser(final BigInteger userId, final String accountName, final String company) {
        tUserRepository.findByAccountNameAndCompanyIgnoreSystemManaged(accountName, company).ifPresent((entity) -> {
            // 会社コードとアカウント名が重複するユーザが存在する場合
            if (!Objects.equals(userId, entity.getId())) {
                // IDが異なる場合は、例外を投げる
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_006));
            }
        });
    }

    /**
     * {@link MaintUserModel} を {@link MUserEntity} にコピーする.
     *
     * @param model {@link MaintUserModel} instance
     * @param entity {@link MUserEntity} instance
     * @throws BusinessException CODE_004 : JUN権限以外で会社コードが設定されていない場合
     */
    public void copyModelToEntity(final MaintUserModel model, final MUserEntity entity) {
        entity.setAccountName(model.getAccountName());
        entity.setEnabled(model.isEnabled());

        if (StringUtils.isNotEmpty(model.getPassword())) {
            entity.setPassword(PASSWORD_ENCODER.encode(model.getPassword()));
        }

        entity.setAuthority(convertAuthoritiesToAuthority(model.getAuthorities()));

        if (model.getAuthorities().stream().anyMatch(authority -> AuthorityType.ROLE_JUN == authority)) {
            // JUN権限の場合、JUNの会社コードを固定で設定
            entity.setCompany(propertyComponent.getCommonProperty().getJunCompany());
        } else {
            if (StringUtils.isEmpty(model.getCompany())) {
                // 会社コードが設定されていない場合は、例外を投げる
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_004));
            }

            entity.setCompany(model.getCompany());
        }

        entity.setName(model.getName());
        entity.setMailAddress(model.getMailAddress());
    }

    /**
     * {@link MUserEntity} を {@link MaintUserModel} にコピーする.
     *
     * @param entity {@link MUserEntity} instance
     * @param model {@link MaintUserModel} instance
     */
    public void copyEntityToModel(final MUserEntity entity, final MaintUserModel model) {
        model.setId(entity.getId());
        model.setAccountName(entity.getAccountName());
        model.setEnabled(entity.isEnabled());
        model.setAuthorities(convertAuthorityToAuthorities(entity.getAuthority()));
        model.setCompany(entity.getCompany());
        model.setName(entity.getName());
        model.setMailAddress(entity.getMailAddress());

        if (!StringUtils.equals(propertyComponent.getCommonProperty().getJunCompany(), model.getCompany())) {
            // 会社コード != JUNの場合、仕入先マスタを検索
            mSirmstRepository.findBySire(entity.getCompany()).ifPresent((mSirmstEntity) -> {
                // レコードがある場合は、名称を設定
                model.setMakerName(mSirmstEntity.getName());
            });
        }
    }

    /**
     * {@link UserEntity} を {@link MaintUserSearchResultModel} にコピーする.
     *
     * @param entity {@link UserEntity} instance
     * @param model {@link MaintUserSearchResultModel} instance
     */
    public void copyEntityToModel(final UserEntity entity, final MaintUserSearchResultModel model) {
        model.setId(entity.getId());
        model.setAccountName(entity.getAccountName());
        model.setEnabled(entity.isEnabled());
        model.setAuthorities(convertAuthorityToAuthorities(entity.getAuthority()));
        model.setCompany(entity.getCompany());
        model.setName(entity.getName());
        model.setMailAddress(entity.getMailAddress());
        model.setMakerName(entity.getMakerName());
    }

    /**
     * 権限リストから重複を除外して、カンマ区切りの権限に変換する.
     *
     * @param values 権限リスト
     * @return {@link AuthorityType} instance
     */
    public String convertAuthoritiesToAuthority(final List<AuthorityType> values) {
        return values
                .stream()
                .distinct()
                .map(authority -> authority.getValue())
                .collect(Collectors.joining(AUTHORITY_SEPARATOR));
    }

    /**
     * カンマ区切りの権限を権限リストに変換する.
     *
     * @param value カンマ区切りの権限
     * @return {@link AuthorityType} instance
     */
    public List<AuthorityType> convertAuthorityToAuthorities(final String value) {
        return Arrays.asList(StringUtils.split(value, AUTHORITY_SEPARATOR))
                .stream()
                .map(authority -> AuthorityType.convertToType(authority))
                .collect(Collectors.toList());
    }
}
