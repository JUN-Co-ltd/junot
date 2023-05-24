package jp.co.jun.edi.component;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MFAvailableCompanyEntity;
import jp.co.jun.edi.repository.MFAvailableCompanyRepository;

/**
 * フクキタル関連のコンポーネント.
 */
@Component
public class FukukitaruComponent extends GenericComponent {
    @Autowired
    private MFAvailableCompanyRepository mfAvailableCompanyRepository;

    /**
     * ログインユーザの所属会社とブランドコードが、フクキタル資材発注が利用可能であるかチェックする.
     * @param company ログインユーザの会社コード
     * @param brandCode ブランドコード
     * @return 利用可能な場合trueを返す。そうでない場合falseを返す
     */
    public boolean isMaterialOrderAvailable(final String company, final String brandCode) {
        if (mfAvailableCompanyRepository.findByBrandCodeAndCompany(brandCode, company).isPresent()) {
            return true;
        }
        return false;
    }

    /**
     * ログインユーザの所属会社とブランドコードが、フクキタル資材発注が利用可能であるかチェックする.
     * @param listMFBrandCampanyEntity フクキタル用ブランドコード別会社情報リスト
     * @param brandCode ブランドコード
     * @return 利用可能な場合trueを返す。そうでない場合falseを返す
     */
    public boolean isMaterialOrderAvailable(final List<MFAvailableCompanyEntity> listMFBrandCampanyEntity, final String brandCode) {
        if (Objects.isNull(listMFBrandCampanyEntity) || listMFBrandCampanyEntity.isEmpty()) {
            return false;
        }
        if (Objects.isNull(brandCode)) {
            return false;
        }
        if (listMFBrandCampanyEntity.stream().filter(entity -> entity.getBrandCode().equals(brandCode)).count() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 会社コードから、フクキタル用ブランドコード別会社情報を取得する.
     * @param company 会社コード
     * @return {@list MFAvailableCompanyEntity} instance
     */
    public List<MFAvailableCompanyEntity> findByCompany(final String company) {
        return mfAvailableCompanyRepository.findByCompany(company, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

}
