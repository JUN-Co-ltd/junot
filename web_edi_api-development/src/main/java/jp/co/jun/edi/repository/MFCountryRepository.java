package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import jp.co.jun.edi.entity.MFCountryEntity;

/**
 * フクキタル用原産国表記マスタを検索するリポジトリ.
 */
public interface MFCountryRepository extends JpaRepository<MFCountryEntity, BigInteger>, JpaSpecificationExecutor<MFCountryEntity>  {
    /**
     * フクキタル用原産国表記マスタ検索.
     *
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFCountryEntity t"
            + " WHERE t.deletedAt IS NULL")
    Page<MFCountryEntity> findByIds(Pageable pageable);
}
