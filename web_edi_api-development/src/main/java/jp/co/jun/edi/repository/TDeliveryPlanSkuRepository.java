package jp.co.jun.edi.repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TDeliveryPlanSkuEntity;

/**
 *
 * TDeliveryPlanSkuRepository.
 *
 */
@Repository
public interface TDeliveryPlanSkuRepository extends JpaRepository<TDeliveryPlanSkuEntity, BigInteger> {

    /**
     * 納品予定明細IDから納品予定SKU を検索する.
     *
     * @param deliveryPlanDetailId 納品予定明細ID
     * @param pageable pageable
     * @return 納品予定SKU
     */
    @Query("SELECT t FROM TDeliveryPlanSkuEntity t"
            + " WHERE t.deliveryPlanDetailId = :deliveryPlanDetailId "
            + " AND t.deletedAt IS NULL")
    Page<TDeliveryPlanSkuEntity> findByDeliveryPlanDetailId(
            @Param("deliveryPlanDetailId") BigInteger deliveryPlanDetailId,
            Pageable pageable);

    /**
     * 除外IDリストと納品予定明細IDをキーに納品予定SKUテーブルから論理削除する.
     * @param deliveryPlanDetailId 納品予定明細ID
     * @param ids 除外IDリスト
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_plan_sku t"
            + " SET t.deleted_at = now() ,"
            + " t.updated_user_id = :updatedUserId ,"
            + " t.updated_at = now()"
            + " WHERE t.delivery_plan_detail_id = :deliveryPlanDetailId "
            + " AND t.deleted_at IS NULL "
            + " AND t.id NOT IN (:ids)", nativeQuery = true)
    int updateDeleteAtByDeliveryPlanDetailIdAndIds(
            @Param("deliveryPlanDetailId") BigInteger deliveryPlanDetailId,
            @Param("ids") List<BigInteger> ids,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 納品予定IDから納品予定SKUの合計値を検索する.
     * @param deliveryPlanId 納品予定ID
     * @return 納品予定数合計
     */
    @Query("SELECT SUM(ds.deliveryPlanLot) as deliveryPlanLot FROM TDeliveryPlanSkuEntity ds"
            + " INNER JOIN TDeliveryPlanDetailEntity dd"
            + " ON ds.deliveryPlanDetailId = dd.id"
            + " WHERE dd.deliveryPlanId = :deliveryPlanId"
            + " AND dd.deletedAt IS NULL")
    BigDecimal getSumDeliveryPlanLotByDeliveryPlanId(
            @Param("deliveryPlanId") BigInteger deliveryPlanId);
}
