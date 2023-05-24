package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TOrderSkuEntity;

/**
 *
 * TOrderSkuRepository.
 * orderNumberは受注確定されるまで、"000000"が設定されるため.
 * orderNumberを条件にしてSQLを実行すると想定外のレコードも.
 * 編集される可能性があるので、orderNumberは条件に極力使用しないこと.
 *
 */
@Repository
public interface TOrderSkuRepository extends JpaRepository<TOrderSkuEntity, BigInteger> {

    /**
     * 発注IDから 発注SKU情報リスト を検索する.
     *
     * @param orderId orderId
     * @param pageable pageable
     * @return 発注SKU情報リストを取得する
     */
    @Query("SELECT t FROM TOrderSkuEntity t"
            + " WHERE t.orderId = :orderId AND t.deletedAt is null")
    Page<TOrderSkuEntity> findByOrderId(@Param("orderId") BigInteger orderId, Pageable pageable);

    /**
     * 発注IDに紐づく発注SKU情報から.
     * 月末日(当月、前月、前々月)が全てある先頭の1レコードを取得する
     *
     * @param orderId orderId
     * @return 発注SKU情報リストを取得する
     */
    @Query(value = "SELECT t.* FROM t_order_sku t"
            + " WHERE t.order_id = :orderId"
            + " AND t.month_end_at is not null"
            + " AND t.previous_month_end_at is not null"
            + " AND t.month_before_end_at is not null"
            + " ORDER BY t.id"
            + " LIMIT 1", nativeQuery = true)
    Optional<TOrderSkuEntity> findFirstExistEndAtOrderSkuByOrderId(@Param("orderId") BigInteger orderId);

    /**
     * 発注IDリストから 発注SKU情報リスト を検索する.
     *
     * @param orderIds 発注IDリスト
     * @return 発注SKU情報リストを取得する
     */
    @Query("SELECT t FROM TOrderSkuEntity t"
            + " WHERE t.orderId IN (:orderIds)"
            + " AND t.deletedAt is null")
    List<TOrderSkuEntity> findByOrderIdList(@Param("orderIds") List<BigInteger> orderIds);

    /**
     * 発注ID,除外発注ID配列から発注SKU情報を論理削除する.
     *
     * @param orderId 発注ID
     * @param excludeOrderIdList 除外発注ID配列
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_sku s"
            + " SET s.deleted_at = now(),"
            + " s.updated_user_id = :updatedUserId,"
            + " s.updated_at = now()"
            + " WHERE s.id NOT IN (:excludeOrderIdList)"
            + " AND s.order_id = :orderId"
            + " AND s.deleted_at IS NULL", nativeQuery = true)
    int updateSkuDeletedAtByOrderIdAndExclusionIds(
            @Param("orderId") BigInteger orderId,
            @Param("excludeOrderIdList") List<BigInteger> excludeOrderIdList,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * idで論理削除されていない発注SKU情報を取得する.
     * @param id id
     * @return 発注SKU情報
     */
    Optional<TOrderSkuEntity> findByIdAndDeletedAtIsNull(@Param("id") BigInteger id);

    /**
     * 発注数で裁断数を更新する.
     *
     * @param orderId 発注ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_sku s"
            + " SET s.product_cut_lot = s.product_order_lot,"
            + " s.updated_user_id = :updatedUserId,"
            + " s.updated_at = now()"
            + " WHERE s.order_id = :orderId"
            + " AND s.deleted_at IS NULL", nativeQuery = true)
    int updateproductCutLot(
            @Param("orderId") BigInteger orderId,
            @Param("updatedUserId") BigInteger updatedUserId);

    // PRD_0145 #10776 add JFE start
    /**
     * 発注ID、色コード、サイズを基に裁断数を更新する.
     *
     * @param orderId 発注ID
     * @param colorCode 色コード
     * @param size サイズ
     * @param productCutLot 裁断数
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_sku s"
            + " SET s.product_cut_lot = :productCutLot,"
            + " s.updated_user_id = :updatedUserId,"
            + " s.updated_at = now()"
            + " WHERE s.order_id = :orderId"
            + " AND s.color_code = :colorCode"
            + " AND s.size = :size"
            + " AND s.deleted_at IS NULL", nativeQuery = true)
    int updateProductCutLotByOrderIdAndColorCodeAndSize(
            @Param("orderId") BigInteger orderId,
            @Param("colorCode") String colorCode,
            @Param("size") String size,
            @Param("productCutLot") int productCutLot,
            @Param("updatedUserId") BigInteger updatedUserId);
    // PRD_0145 #10776 add JFE end

    /**
     * 発注ID、色コード、サイズを基に発注SKU情報を取得する.
     *
     * @param orderId 発注ID
     * @param colorCode 色コード
     * @param size サイズ
     * @return 発注SKU情報
     */
    @Query("SELECT t FROM TOrderSkuEntity t"
            + " WHERE t.orderId = :orderId"
            + " AND t.colorCode = :colorCode"
            + " AND t.size = :size"
            + " AND t.deletedAt IS NULL")
    Optional<TOrderSkuEntity> findByOrderIdAndColorAndSize(
            @Param("orderId") BigInteger orderId,
            @Param("colorCode") String colorCode,
            @Param("size") String size);

    /**
     * 発注ID、色コード、サイズを基に発注SKU情報を取得する.
     * + t_orderをINNER JOIN して存在チェックを実施
     *
     * @param orderId 発注Id
     * @param colorCode 色
     * @param size サイズ
     * @return 発注SKU情報
     */
    @Query(value = "SELECT t.* FROM t_order_sku t "
            + " INNER JOIN t_order odr "
            + "    ON odr.id = t.order_id "
            + "   AND odr.deleted_at IS NULL "
            + " WHERE t.order_id = :orderId "
            + "   AND t.color_code = :colorCode "
            + "   AND t.size = :size "
            + "   AND t.deleted_at IS NULL ", nativeQuery = true)
    Optional<TOrderSkuEntity> findByOrderIdAndColorAndSizeWithTOrderCheck(
            @Param("orderId") BigInteger orderId,
            @Param("colorCode") String colorCode,
            @Param("size") String size);

    /**
     * 納品依頼数量に承認対象の納品SKU情報の納品数量を加算して更新する.
     *
     * @param deliveryId 納品ID
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_sku tos"
            + " , ("
            + "   SELECT"
            + "     td.order_id"
            + "     , tds.color_code"
            + "     , tds.size"
            + "     , SUM(tds.delivery_lot) total_delivery_lot"
            + "   FROM"
            + "     t_delivery_sku tds"
            + "     INNER JOIN t_delivery_detail tdd"
            + "       ON tds.delivery_detail_id = tdd.id"
            + "       AND tdd.delivery_id = :deliveryId"
            + "       AND tdd.deleted_at IS NULL"
            + "     INNER JOIN t_delivery td"
            + "       ON td.id = tdd.delivery_id"
            + "       AND td.deleted_at IS NULL"
            + "       AND td.id = :deliveryId"
            + "   WHERE"
            + "     tds.deleted_at IS NULL"
            + "   GROUP BY"
            + "     tds.color_code"
            + "     , tds.size"
            + " ) d"
            + " SET"
            + "   tos.delivery_lot = (tos.delivery_lot + d.total_delivery_lot)"
            + "   , tos.updated_user_id = :userId"
            + "   , tos.updated_at = now()"
            + " WHERE"
            + "   tos.order_id = d.order_id"
            + "   AND tos.color_code = d.color_code"
            + "   AND tos.size = d.size"
            + "   AND tos.deleted_at IS NULL", nativeQuery = true)
    int addDeliveryLotByDeliveryApprove(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("userId") BigInteger userId);

    /**
     * 納品依頼数量に削除対象の納品SKU情報の納品数量を減算して更新する.
     *
     * @param deliveryId 納品ID
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_sku tos"
            + " , ("
            + "   SELECT"
            + "     td.order_id"
            + "     , tds.color_code"
            + "     , tds.size"
            + "     , SUM(tds.delivery_lot) total_delivery_lot"
            + "   FROM"
            + "     t_delivery_sku tds"
            + "     INNER JOIN t_delivery_detail tdd"
            + "       ON tds.delivery_detail_id = tdd.id"
            + "       AND tdd.delivery_id = :deliveryId"
            + "       AND tdd.deleted_at IS NULL"
            + "     INNER JOIN t_delivery td"
            + "       ON td.id = tdd.delivery_id"
            + "       AND td.deleted_at IS NULL"
            + "       AND td.id = :deliveryId"
            + "   WHERE"
            + "     tds.deleted_at IS NULL"
            + "   GROUP BY"
            + "     tds.color_code"
            + "     , tds.size"
            + " ) d"
            + " SET"
            + "   tos.delivery_lot = (tos.delivery_lot - d.total_delivery_lot)"
            + "   , tos.updated_user_id = :userId"
            + "   , tos.updated_at = now()"
            + " WHERE"
            + "   tos.order_id = d.order_id"
            + "   AND tos.color_code = d.color_code"
            + "   AND tos.size = d.size"
            + "   AND tos.deleted_at IS NULL", nativeQuery = true)
    int subtractDeliveryLotByDeliveryDelete(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("userId") BigInteger userId);

    /**
     * 納品依頼数量を更新する.
     *
     * @param deliveryLot 納品依頼数量
     * @param orderSkuId 発注SKUID
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_sku tos"
            + " SET"
            + "   tos.delivery_lot = :deliveryLot"
            + "   , tos.updated_user_id = :userId"
            + "   , tos.updated_at = now()"
            + " WHERE"
            + "   tos.id = :orderSkuId"
            + "   AND tos.deleted_at IS NULL", nativeQuery = true)
    int updateDeliveryLot(
            @Param("deliveryLot") int deliveryLot,
            @Param("orderSkuId") BigInteger orderSkuId,
            @Param("userId") BigInteger userId);
}
