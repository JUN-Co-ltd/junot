package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MFRecycleEntity;

/**
 * フクキタル用リサイクルを検索するリポジトリ.
 */
public interface MFRecycleRepository extends JpaRepository<MFRecycleEntity, BigInteger>, JpaSpecificationExecutor<MFRecycleEntity> {
    /**
     * フクキタル用リサイクルマスタ検索.
     *
     * @param idList IDリスト
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFRecycleEntity t"
            + " WHERE t.id IN :idList"
            + " AND t.deletedAt IS NULL")
    Page<MFRecycleEntity> findByIds(
            @Param("idList") List<BigInteger> idList, Pageable pageable);

    /**
     * フクキタル用リサイクルマスタ検索.
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFRecycleEntity t"
            + " WHERE t.deletedAt IS NULL")
    Page<MFRecycleEntity> findByAll(
            Pageable pageable);
}
