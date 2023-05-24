package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MFAvailableCompanyEntity;

/**
 * フクキタル用ブランドコード別会社情報マスタを検索するリポジトリ.
 */
public interface MFAvailableCompanyRepository extends JpaRepository<MFAvailableCompanyEntity, BigInteger>, JpaSpecificationExecutor<MFAvailableCompanyEntity> {
    /**
     * フクキタル用ブランドコード別会社情報を取得.
     * @param brandCode ブランドコード
     * @param company メーカーコード
     * @return フクキタル品番情報
     */
    @Query("SELECT t FROM MFAvailableCompanyEntity t"
            + " WHERE t.deletedAt is null"
            + " AND t.brandCode = :brandCode"
            + " AND t.company = :company"
            + " AND t.enabled = 1")
    Optional<MFAvailableCompanyEntity> findByBrandCodeAndCompany(
            @Param("brandCode") String brandCode,
            @Param("company") String company);

    /**
     * メーカーコードでフクキタル利用可能なブランドの一覧を取得.
     * @param company メーカーコード
     * @param page {@link Pageable} instace
     * @return フクキタル品番情報
     */
    @Query("SELECT t FROM MFAvailableCompanyEntity t"
            + " WHERE t.deletedAt is null"
            + " AND t.company = :company"
            + " AND t.enabled = 1")
    Page<MFAvailableCompanyEntity> findByCompany(
            @Param("company") String company, Pageable page);
}
