package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MFCnProductCategoryEntity;

/**
 * フクキタル用中国内販情報製品分類マスタを検索するリポジトリ.
 */
public interface MFCnProductCategoryRepository
        extends JpaRepository<MFCnProductCategoryEntity, BigInteger>, JpaSpecificationExecutor<MFCnProductCategoryEntity> {
    /**
     * idsに該当するフクキタル用中国内販情報製品分類マスタ情報を検索.
     *
     * @param idList IDリスト
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFCnProductCategoryEntity t"
            + " WHERE t.id IN :idList"
            + " AND t.deletedAt IS NULL")
    Page<MFCnProductCategoryEntity> findByIds(
            @Param("idList") List<BigInteger> idList, Pageable pageable);

    /**
     * フクキタル用中国内販情報製品分類マスタ情報検索.
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFCnProductCategoryEntity t"
            + " WHERE t.deletedAt IS NULL")
    Page<MFCnProductCategoryEntity> findByDeletedAtIsNull(
            Pageable pageable);
}
