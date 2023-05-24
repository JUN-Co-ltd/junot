package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TCompositionEntity;

/**
 *
 * TCompositionRepository.
 *
 */
@Repository
public interface TCompositionRepository extends JpaRepository<TCompositionEntity, BigInteger> {
    /**
     * 品番IDから 組成情報 を検索する.
     *
     * @param partNoId
     *            品番ID
     * @param pageable pageable
     * @return 組成情報を取得する
     */
    @Query("SELECT t FROM TCompositionEntity t" + " WHERE t.id.partNoId = :partNoId AND t.deletedAt is null ")
    Page<TCompositionEntity> findByPartNoId(@Param("partNoId") BigInteger partNoId, Pageable pageable);

    /**
     * 品番IDから 組成情報 リストを取得する.
     *
     * @param partNoId 品番ID
     * @return 組成情報を取得する
     */
    @Query(value = "SELECT t.* FROM t_composition t"
            + " WHERE t.part_no_id = :partNoId"
            + " AND t.deleted_at is null", nativeQuery = true)
    List<TCompositionEntity> findCompositionListByPartNoId(@Param("partNoId") BigInteger partNoId);

    /**
     * 品番ID,除外ID配列から組成情報を取得する.
     * @param partNoId 品番ID
     * @param ids 除外ID配列
     * @param pageable pageable
     * @return 組成情報を取得する
     */
    @Query("SELECT t FROM TCompositionEntity t"
            + " WHERE t.partNoId = :partNoId"
            + " AND t.id NOT IN (:ids)"
            + " AND t.deletedAt is null")
    Page<TCompositionEntity> findByPartNoIdAndIds(
            @Param("partNoId") BigInteger partNoId,
            @Param("ids") List<BigInteger> ids,
            Pageable pageable);
}
