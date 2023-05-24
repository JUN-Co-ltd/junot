package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MFSealEntity;

/**
 * フクキタル用シールを検索するリポジトリ.
 */
public interface MFSealRepository extends JpaRepository<MFSealEntity, BigInteger>, JpaSpecificationExecutor<MFSealEntity> {
    /**
     * フクキタル用シールマスタ検索.
     *
     * @param idList IDリスト
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFSealEntity t"
            + " WHERE t.id IN :idList"
            + " AND t.deletedAt IS NULL")
    Page<MFSealEntity> findByIds(
            @Param("idList") List<BigInteger> idList, Pageable pageable);

    /**
     * フクキタル用シールマスタ検索.
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFSealEntity t"
            + " WHERE t.deletedAt IS NULL")
    Page<MFSealEntity> findByAllDeletedAtIsNull(
            Pageable pageable);
}
