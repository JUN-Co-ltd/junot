package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TSkuEntity;
/**
 *
 * TSkuRepository.
 *
 */
@Repository
public interface TSkuRepository extends JpaRepository<TSkuEntity, BigInteger> {
    /**
     * 品番IDをキーにSKU情報を検索する.
     * @param partNoId 品番ID
     * @param pageable pageable
     * @return SKU情報を取得する
     */
    @Query("SELECT t FROM TSkuEntity t" + " WHERE t.partNoId = :partNoId AND t.deletedAt is null")
    Page<TSkuEntity> findByPartNoId(@Param("partNoId") BigInteger partNoId, Pageable pageable);

    /**
     * 品番IDをキーにSKU情報を検索する.
     * @param partNoId 品番ID
     * @return SKU情報を取得する
     */
    @Query("SELECT t FROM TSkuEntity t" + " WHERE t.partNoId = :partNoId AND t.deletedAt is null")
    List<TSkuEntity> findByPartNoId(@Param("partNoId") BigInteger partNoId);

    /**
     * 品番ID,除外ID配列からSKU情報を取得する.
     * @param partNoId 品番ID
     * @param ids 除外ID配列
     * @param pageable pageable
     * @return SKU情報を取得する
     */
    @Query("SELECT t FROM TSkuEntity t"
            + " WHERE t.partNoId = :partNoId"
            + " AND t.id NOT IN (:ids)"
            + " AND t.deletedAt is null")
    Page<TSkuEntity> findByPartNoIdAndIds(
            @Param("partNoId") BigInteger partNoId,
            @Param("ids") List<BigInteger> ids,
            Pageable pageable);

    /**
     * JANコードからSKUを取得する.
     * @param janCode JANコード
     * @return SKU情報を取得する
     */
    @Query("SELECT t FROM TSkuEntity t"
            + " WHERE t.janCode = :janCode"
            + " AND t.deletedAt is null")
    Optional<TSkuEntity> findByJanCode(
            @Param("janCode") String janCode);
}
