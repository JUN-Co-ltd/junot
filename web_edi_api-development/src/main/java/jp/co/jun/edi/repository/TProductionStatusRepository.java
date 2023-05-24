package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TProductionStatusEntity;

/**
 * TProductionStatusRepository.
 */
@Repository
public interface TProductionStatusRepository extends JpaRepository<TProductionStatusEntity, BigInteger>, JpaSpecificationExecutor<TProductionStatusEntity> {

    /**
     * 発注Idを基に生産ステータス情報を取得する.
     * @param orderId 発注Id
     * @return 発注情報
     */
    @Query("SELECT t FROM TProductionStatusEntity t"
            + " WHERE t.orderId = :orderId"
            + " AND t.deletedAt is null")
    Optional<TProductionStatusEntity> findByOrderId(
            @Param("orderId") BigInteger orderId);

    /**
     * 発注IDを基に生産ステータスの発注Noを更新する.
     *
     * @param orderId 発注ID
     * @param orderNo 発注No
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_production_status p"
            + " SET p.order_number = :orderNo,"
            + " p.updated_user_id = :updatedUserId,"
            + " p.updated_at = now()"
            + " WHERE p.order_id = :orderId"
            + " AND p.deleted_at IS NULL", nativeQuery = true)
    int updateOrderNoByOrderId(
            @Param("orderId") BigInteger orderId,
            @Param("orderNo") BigInteger orderNo,
            @Param("updatedUserId") BigInteger updatedUserId);

}
