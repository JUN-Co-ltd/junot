package jp.co.jun.edi.repository;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TDeliveryPlanCutEntity;

/**
 *
 * TDeliveryPlanCutRepository.
 *
 */
@Repository
public interface TDeliveryPlanCutRepository extends JpaRepository<TDeliveryPlanCutEntity, BigInteger> {

    /**
     * 納品予定IDから納品予定裁断を検索する.
     *
     * @param deliveryPlanId 納品予定ID
     * @param pageable pageable
     * @return 納品予定裁断
     */
    @Query("SELECT t FROM TDeliveryPlanCutEntity t"
            + " WHERE t.deliveryPlanId = :deliveryPlanId "
            + " AND t.deletedAt IS NULL")
    Page<TDeliveryPlanCutEntity> findByDeliveryPlanId(
            @Param("deliveryPlanId") BigInteger deliveryPlanId,
            Pageable pageable);

    /**
     * 納品予定IDから裁断数の合計値を検索する.
     * @param deliveryPlanId 納品予定ID
     * @return 納品予定数合計
     */
    @Query("SELECT SUM(t.deliveryPlanCutLot) as deliveryPlanLot FROM TDeliveryPlanCutEntity t"
            + " WHERE t.deliveryPlanId = :deliveryPlanId "
            + " AND t.deletedAt IS NULL")
    BigDecimal getSumDeliveryPlanCutLotByDeliveryPlanId(
            @Param("deliveryPlanId") BigInteger deliveryPlanId);
}
