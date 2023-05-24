package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TExternalSkuEntity;

/**
 *
 * TExternalSkuRepository.
 *
 */
@Repository
public interface TExternalSkuRepository extends JpaRepository<TExternalSkuEntity, BigInteger> {
    /**
     * 品番IDをキーに外部SKUを検索する.
     * @param partNoId 品番ID
     * @param pageable pageable
     * @return 外部SKUを取得する
     */
    @Query("SELECT t FROM TExternalSkuEntity t" + " WHERE t.partNoId = :partNoId AND t.deletedAt is null")
    Page<TExternalSkuEntity> findByPartNoId(@Param("partNoId") BigInteger partNoId, Pageable pageable);
}
