package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TProductionStatusHistoryEntity;

/**
 *
 * TProductionStatusHistoryRepository.
 *
 */
@Repository
public interface TProductionStatusHistoryRepository
                extends JpaRepository<TProductionStatusHistoryEntity, BigInteger>, JpaSpecificationExecutor<TProductionStatusHistoryEntity>  {
    /**
     * 発注IDから 生産ステータス履歴 を検索する.
     *
     * @param orderId orderId
     * @param pageable pageable
     * @return 生産ステータス履歴リスト
     */
    @Query("SELECT t FROM TProductionStatusHistoryEntity t" + " WHERE t.orderId = :orderId AND t.deletedAt is null")
    Page<TProductionStatusHistoryEntity> findByOrderId(@Param("orderId") BigInteger orderId, Pageable pageable);
}
