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

import jp.co.jun.edi.entity.TDeliveryStoreSkuEntity;

/**
 *
 * TDeliveryStoreSkuRepository.
 *
 */
@Repository
public interface TDeliveryStoreSkuRepository extends JpaRepository<TDeliveryStoreSkuEntity, BigInteger> {

    /**
     * 納品得意先IDをキーに納品得意先SKU情報を検索する.
     *
     * @param deliveryStoreId 納品得意先ID
     * @param pageable pageable
     * @return 納品得意先SKU情報
     */
    @Query("SELECT t FROM TDeliveryStoreSkuEntity t"
            + " WHERE t.deliveryStoreId = :deliveryStoreId"
            + " AND t.deletedAt IS NULL")
    Page<TDeliveryStoreSkuEntity> findByDeliveryStoreId(
            @Param("deliveryStoreId") BigInteger deliveryStoreId,
            Pageable pageable);

    /**
     * 納品明細IDリストをキーに納品得意先SKU情報を論理削除する.
     *
     * @param ids 納品明細IDリスト
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_store_sku sk"
            + " SET sk.deleted_at = now()"
            + "     ,sk.updated_user_id = :updatedUserId"
            + "     ,sk.updated_at = now()"
            + " WHERE sk.deleted_at IS NULL"
            + " AND sk.delivery_store_id IN ("
            + "    SELECT st.id"
            + "      FROM t_delivery_store st"
            + "     WHERE st.delivery_detail_id IN (:ids)"
            + "   )", nativeQuery = true)
    int updateDeletedAtByDeliveryDetailIds(
            @Param("ids") List<BigInteger> ids,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 納品得意先ID,除外納品得意先SKUID配列をキーに納品得意先SKU情報を論理削除する.
     *
     * @param deliveryStoreId 納品得意先ID
     * @param excludeDeliveryStoreSkuIds 除外納品得意先SKUID配列
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_store_sku t"
            + " SET t.deleted_at = now(),"
            + " t.updated_user_id = :updatedUserId,"
            + " t.updated_at = now()"
            + " WHERE t.delivery_store_id = :deliveryStoreId"
            + " AND t.id NOT IN (:excludeDeliveryStoreSkuIds)"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    int updateDeletedAtByDeliveryStoreIdAndExclusionIds(
            @Param("deliveryStoreId") BigInteger deliveryStoreId,
            @Param("excludeDeliveryStoreSkuIds") List<BigInteger> excludeDeliveryStoreSkuIds,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 納品明細ID,除外納品明細ID配列をキーに納品得意先SKU情報を論理削除する.
     *
     * @param deliveryDetailId 納品明細ID
     * @param excludeDeliveryStoreIds 除外納品得意先ID配列
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_store_sku sk"
            + "   LEFT JOIN t_delivery_store st "
            + "          ON sk.delivery_store_id = st.id "
            + " SET sk.deleted_at = now(),"
            + " sk.updated_user_id = :updatedUserId,"
            + " sk.updated_at = now()"
            + " WHERE sk.delivery_store_id NOT IN (:excludeDeliveryStoreIds)"
            + " AND st.delivery_detail_id = :deliveryDetailId"
            + " AND sk.deleted_at IS NULL", nativeQuery = true)
    int updateDeletedAtByDeliveryDetailIdAndExclusionStoreIds(
            @Param("deliveryDetailId") BigInteger deliveryDetailId,
            @Param("excludeDeliveryStoreIds") List<BigInteger> excludeDeliveryStoreIds,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 納品IDをキーに納品得意先SKU情報を論理削除する.
     *
     * @param deliveryId 納品ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_store_sku sk"
            + "   LEFT JOIN t_delivery_store st "
            + "          ON st.id = sk.delivery_store_id"
            + "   LEFT JOIN t_delivery_detail dd "
            + "          ON dd.id = st.delivery_detail_id"
            + " SET sk.deleted_at = now()"
            + "     ,sk.updated_user_id = :updatedUserId"
            + "     ,sk.updated_at = now()"
            + " WHERE dd.delivery_id = :deliveryId"
            + "   AND sk.deleted_at IS NULL", nativeQuery = true)
    int updateDeletedAtByDeliveryId(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 倉庫連携ファイルIDをキーに納品得意先SKU情報の件数を検索する.
     * @param wmsLinkingFileId 倉庫連携ファイルID
     * @return 取得件数
     */
    @Query(value = " SELECT "
            + " COUNT(sku.id) "
            + " FROM t_delivery_store_sku sku "
            + " WHERE sku.wms_linking_file_id = :wmsLinkingFileId "
            + " AND sku.deleted_at IS NULL ", nativeQuery = true)
    int countByWmsLinkingFileId(@Param("wmsLinkingFileId") BigInteger wmsLinkingFileId);

    /**
     * idで論理削除されていない納品得意先SKU情報を取得する.
     * @param id id
     * @return 納品得意先SKU情報
     */
    Optional<TDeliveryStoreSkuEntity> findByIdAndDeletedAtIsNull(@Param("id") BigInteger id);

    /**
     * 配分出荷指示確定ファイルから更新対象の得意先SKU情報を検索.
     * @param size サイズ
     * @param colorCode カラーコード
     * @param storeCode 店舗コード
     * @param divisionCode 課コード
     * @param orderNumber 発注番号
     * @param deliveryCount 納品依頼回数
     * @param partNo 品番
     * @return 配分出荷指示情報
     */
    @Query(value = "SELECT t.*"
            + "     FROM t_delivery_store_sku t"
            + "         INNER JOIN t_delivery_store tds"
            + "             ON tds.id = t.delivery_store_id"
            + "             AND tds.deleted_at IS NULL"
            + "         INNER JOIN t_delivery_detail tdd"
            + "             ON tdd.id = tds.delivery_detail_id"
            + "             AND tds.deleted_at IS NULL"
            + "         INNER JOIN t_delivery td"
            + "             ON td.id = tdd.delivery_id"
            + "             AND td.deleted_at IS NULL"
            + "     WHERE t.size = :size"
            + "         AND t.color_code = :colorCode"
            + "         AND tds.store_code = :storeCode"
            + "         AND tdd.division_code = :divisionCode"
            + "         AND td.order_number = :orderNumber"
            + "         AND td.delivery_count = :deliveryCount"
            + "         AND td.part_no = :partNo"
            + "         AND tdd.allocation_complete_at IS NULL"
            + "         AND t.deleted_at IS NULL", nativeQuery = true)
    Optional<TDeliveryStoreSkuEntity> findByManageColumnAndLineNumber(
            @Param("size") String size,
            @Param("colorCode") String colorCode,
            @Param("storeCode") String storeCode,
            @Param("divisionCode") String divisionCode,
            @Param("orderNumber") BigInteger orderNumber,
            @Param("deliveryCount") int deliveryCount,
            @Param("partNo") String partNo);

    /**
     * 納品IDをキーに納品得意先SKU情報を検索する.
     *
     * @param deliveryId 納品ID
     * @param divisionCode 課コード
     * @return 納品得意先SKU情報(カラーコード/サイズ順に抽出)
     */
    @Query(value = "SELECT "
            + "   * "
            + " FROM t_delivery_store_sku ssku "
            + " INNER JOIN t_delivery_store store "
            + "   ON store.id = ssku.delivery_store_id "
            + "   AND store.deleted_at IS NULL "
            + " INNER JOIN t_delivery_detail detail "
            + "   ON detail.id = store.delivery_detail_id "
            + "   AND detail.deleted_at IS NULL "
            + " WHERE 1=1 "
            + "   AND detail.delivery_id = :deliveryId "
            + "   AND detail.division_code = :divisionCode "
            + "   AND ssku.deleted_at IS NULL "
            + " ORDER BY "
            + "   ssku.color_code"
            + " , ssku.size", nativeQuery = true)
    List<TDeliveryStoreSkuEntity> findByDeliveryIdAndDivisionCode(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("divisionCode") String divisionCode
            );

}
