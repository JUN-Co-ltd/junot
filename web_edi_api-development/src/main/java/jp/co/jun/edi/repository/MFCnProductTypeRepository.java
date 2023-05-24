package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MFCnProductTypeEntity;

/**
 * フクキタル用中国内販情報製品種別マスタを検索するリポジトリ.
 */
public interface MFCnProductTypeRepository extends JpaRepository<MFCnProductTypeEntity, BigInteger>, JpaSpecificationExecutor<MFCnProductTypeEntity> {
    /**
     * フクキタル用中国内販情報製品種別マスタ情報検索.
     *
     * @param idList IDリスト
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFCnProductTypeEntity t"
            + " WHERE t.id IN :idList"
            + " AND t.deletedAt IS NULL")
    Page<MFCnProductTypeEntity> findByIds(
            @Param("idList") List<BigInteger> idList, Pageable pageable);

    /**
     * フクキタル用中国内販情報製品種別マスタ情報検索.
     * @param pageable ページ情報
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFCnProductTypeEntity t"
            + " WHERE t.deletedAt IS NULL")
    Page<MFCnProductTypeEntity> findByDeletedAtIsNull(
            Pageable pageable);
}
