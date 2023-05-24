package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TDeliveryPlanDetailEntity;

/**
 * TDeliveryPlanDetailRepository.
 */
@Repository
public interface TDeliveryPlanDetailRepository extends JpaRepository<TDeliveryPlanDetailEntity, BigInteger> {
   /**
    * 納品予定IDから 納品予定明細 を検索する.
    * 納品予定日の古い順でソートする。納品予定日空白は後ろにする。
    *
    * @param deliveryPlanId deliveryPlanId
    * @param pageable pageable
    * @return 納品予定明細を取得する
    */
    @Query(value = "SELECT t.* "
            + " FROM t_delivery_plan_detail t"
            + " WHERE t.delivery_plan_id = :deliveryPlanId "
            + " AND t.deleted_at IS NULL"
            + " ORDER BY t.delivery_plan_at IS NULL ASC, t.delivery_plan_at ASC, t.id ASC ", nativeQuery = true)
    Page<TDeliveryPlanDetailEntity> findByDeliveryPlanId(
            @Param("deliveryPlanId") BigInteger deliveryPlanId,
            Pageable pageable);

    /**
     * 除外IDリストと納品予定IDをキーに納品予定明細テーブルから論理削除する.
     * @param deliveryPlanId 納品予定ID
     * @param ids 除外IDリスト
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_plan_detail t"
            + " SET t.deleted_at = now() ,"
            + " t.updated_user_id = :updatedUserId ,"
            + " t.updated_at = now()"
            + " WHERE t.delivery_plan_id = :deliveryPlanId "
            + " AND t.deleted_at IS NULL "
            + " AND t.id NOT IN (:ids)", nativeQuery = true)
    int updateDeleteAtByDeliveryPlanIdAndIds(
            @Param("deliveryPlanId") BigInteger deliveryPlanId,
            @Param("ids") List<BigInteger> ids,
            @Param("updatedUserId") BigInteger updatedUserId);
}
