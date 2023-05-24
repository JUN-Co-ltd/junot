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

import jp.co.jun.edi.entity.TDeliverySkuEntity;

/**
 *
 * TDeliverySkuRepository.
 *
 */
@Repository
public interface TDeliverySkuRepository extends JpaRepository<TDeliverySkuEntity, BigInteger> {

    /**
     * 納品明細ID,除外納品SKUID配列から納品SKU情報を論理削除する.
     *
     * @param deliveryDetailId 納品明細ID
     * @param excludeDeliverySkuIds 除外納品SKUID配列
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_sku t"
            + " SET t.deleted_at = now(),"
            + " t.updated_user_id = :updatedUserId,"
            + " t.updated_at = now()"
            + " WHERE t.delivery_detail_id = :deliveryDetailId"
            + " AND t.id NOT IN (:excludeDeliverySkuIds)"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    int updateSkuDeletedAtByDeliveryDetailIdAndExclusionIds(
            @Param("deliveryDetailId") BigInteger deliveryDetailId,
            @Param("excludeDeliverySkuIds") List<BigInteger> excludeDeliverySkuIds,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 納品ID,除外納品明細ID配列から納品SKU情報を論理削除する.
     *
     * @param deliveryId 納品ID
     * @param excludeDeliveryDetailIds 除外納品明細ID配列
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_sku s"
            + "   LEFT JOIN t_delivery_detail d "
            + "          ON s.delivery_detail_id = d.id "
            + " SET s.deleted_at = now(),"
            + " s.updated_user_id = :updatedUserId,"
            + " s.updated_at = now()"
            + " WHERE s.delivery_detail_id NOT IN (:excludeDeliveryDetailIds)"
            + " AND d.delivery_id = :deliveryId"
            + " AND s.deleted_at IS NULL", nativeQuery = true)
    int updateSkuDeletedAtByDeliveryIdAndExclusionIds(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("excludeDeliveryDetailIds") List<BigInteger> excludeDeliveryDetailIds,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 納品明細IDから納品SKU情報 を検索する.
     *
     * @param deliveryDetailId 納品明細ID
     * @param pageable pageable
     * @return 納品SKU情報を取得する
     */
    @Query("SELECT t FROM TDeliverySkuEntity t"
            + " WHERE t.deliveryDetailId = :deliveryDetailId "
            + " AND t.deletedAt IS NULL")
    Page<TDeliverySkuEntity> findByDeliveryDetailId(
            @Param("deliveryDetailId") BigInteger deliveryDetailId,
            Pageable pageable);

    /**
     * ※入荷数量は使ってないので0固定にしています.
     * @param deliveryId 納品ID
     * @return SKUごとに納品数量を集計した納品SKU情報リスト
     */
    @Query(value = "SELECT"
            + "  ds.id"
            + "   , ds.delivery_detail_id"
            + "   , ds.delivery_request_number"
            + "   , ds.division_code"
            + "   , ds.size"
            + "   , ds.color_code"
            + "   , SUM(ds.delivery_lot) AS delivery_lot"
            + "   , 0 AS arrival_lot"
            + "   , ds.created_at"
            + "   , ds.created_user_id"
            + "   , ds.updated_at"
            + "   , ds.updated_user_id"
            + "   , ds.deleted_at"
            + " FROM"
            + "   t_delivery_sku ds"
            + " WHERE"
            + "   ds.deleted_at IS NULL"
            + "   AND EXISTS ("
            + "     SELECT"
            + "       1"
            + "     FROM"
            + "       t_delivery_detail dd"
            + "     WHERE"
            + "       dd.deleted_at IS NULL"
            + "       AND dd.delivery_id = :deliveryId"
            + "       AND dd.id = ds.delivery_detail_id"
            + "   )"
            + " GROUP BY"
            + "   color_code"
            + "   , size", nativeQuery = true)
    Optional<List<TDeliverySkuEntity>> sumDeliveryLotGroupBySku(@Param("deliveryId") BigInteger deliveryId);

    /**
     * 納品明細IDをキーに納品SKU情報の納品依頼Noを更新する.
     *
     * @param deliveryRequestNumber 納品依頼No
     * @param updatedUserId 更新ユーザID
     * @param deliveryDetailId 納品明細ID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_sku t"
            + " SET t.delivery_request_number = ?1,"
            + " t.updated_user_id = ?2,"
            + " t.updated_at = now()"
            + " WHERE t.delivery_detail_id = ?3"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    int updateDeliveryRequestNumberByDeliveryDetailId(
            @Param("deliveryRequestNumber") String deliveryRequestNumber,
            @Param("updatedUserId") BigInteger updatedUserId,
            @Param("deliveryDetailId") BigInteger deliveryDetailId);

    /**
     * 納品明細IDリストから納品SKU情報を論理削除する.
     *
     * @param ids 納品明細IDリスト
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_sku s"
            + " SET s.deleted_at = now(),"
            + " s.updated_user_id = :updatedUserId,"
            + " s.updated_at = now()"
            + " WHERE s.delivery_detail_id IN (:ids)"
            + " AND s.deleted_at IS NULL", nativeQuery = true)
    int updateSkuDeletedAtByDeliveryDetailIds(
            @Param("ids") List<BigInteger> ids,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * idで論理削除されていない納品SKU情報を取得する.
     * @param id id
     * @return 納品SKU情報
     */
    Optional<TDeliverySkuEntity> findByIdAndDeletedAtIsNull(@Param("id") BigInteger id);

    /**
     * 仕入情報を基に納品SKUを検索する.
     * @param deliveryId 納品ID
     * @param deliveryCount 納品依頼回数
     * @param divisionCode 課コード
     * @param colorCode 色コード
     * @param size サイズ
     * @return 納品SKU情報
     */
    @Query(value = "SELECT ds.* FROM t_delivery_sku ds"
            + " INNER JOIN t_delivery_detail dd"
            + "       ON ds.delivery_detail_id = dd.id"
            + "       AND dd.delivery_id = :deliveryId"
            + "       AND dd.delivery_count = :deliveryCount"
            + "       AND dd.division_code = :divisionCode"
            + "       AND dd.deleted_at IS NULL"
            + " WHERE ds.color_code = :colorCode"
            + " AND ds.size = :size"
            + " AND ds.deleted_at IS NULL", nativeQuery = true)
    Optional<TDeliverySkuEntity> findByPurchaseInfo(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("deliveryCount") int deliveryCount,
            @Param("divisionCode") String divisionCode,
            @Param("colorCode") String colorCode,
            @Param("size") String size);
}
