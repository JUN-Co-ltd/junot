package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTDeliveryStoreSkuPDFEntity;

/**
 *
 * ExtendedTDeliveryStoreSkuPDFRepository.
 *
 */
@Repository
public interface ExtendedTDeliveryStoreSkuPDFRepository
extends JpaRepository<ExtendedTDeliveryStoreSkuPDFEntity, BigInteger> {

    /**
     * 納品IDをキーに納品得意先SKU情報を検索する.
     * (色及びサイズ単位で納品数・出荷確定数は集計する)
     *
     * @param deliveryId 納品ID
     * @param divisionCode 課コード
     * @return 納品得意先SKU情報(カラーコード/サイズ順に抽出)
     */
    @Query(value = "SELECT "
            + "   ssku.id"
            + " , ssku.delivery_store_id "
            + " , ssku.color_code"
            + " , ssku.size"
            + " , SUM(ssku.delivery_lot) AS delivery_lot "
            + " , SUM(ssku.arrival_lot) AS arrival_lot "
            + " FROM t_delivery_store_sku ssku "
            + " INNER JOIN t_delivery_store store "
            + "   ON store.id = ssku.delivery_store_id "
            + "   AND store.deleted_at IS NULL "
            + " INNER JOIN t_delivery_detail detail "
            + "   ON detail.id = store.delivery_detail_id "
            + "   AND detail.deleted_at IS NULL "
            + " INNER JOIN t_delivery delivery "
            + "   ON delivery.id = detail.delivery_id "
            + "   AND delivery.deleted_at IS NULL "
            + " INNER JOIN "
            + " ( "
            + "    SELECT "
            + "       sm.hscd, sm.jun, sm.szkg "
            + "    FROM m_sizmst sm "
            + "    WHERE 1=1 "
            + "       AND sm.deleted_at IS NULL "
            + " ) sm "
            + "   ON sm.hscd = SUBSTRING(delivery.part_no, 1, 3)"
            + "   AND sm.szkg = ssku.size "
            + " WHERE detail.delivery_id = :deliveryId "
            + "   AND detail.division_code = :divisionCode "
            + "   AND ssku.deleted_at IS NULL "
            + " GROUP BY "
            + "   ssku.color_code "
            + " , ssku.size "
            + " ORDER BY "
            + "   ssku.color_code "
            + " , sm.jun ", nativeQuery = true)
    Optional<List<ExtendedTDeliveryStoreSkuPDFEntity>> findByDeliveryIdAndDivisionCodeGroupByColorCodeAndSize(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("divisionCode") String divisionCode
            );

    /**
     * ピッキングリストPDFヘッダ作成用SQL.
     * ※ ブランド名・課名は別メソッド経由で取得
     *
     * @param deliveryId 納品ID
     * @param storeCode storeCode
     * @return PDFヘッダ情報
     */
    @Query(value = " SELECT "
            + "   ssku.id "
            + " , ssku.delivery_store_id "
            + " , ssku.color_code"
            + " , ssku.size"
            + " , ssku.delivery_lot"
            + " FROM t_delivery_store_sku ssku"
            + " INNER JOIN t_delivery_store store"
            + "   ON store.id = ssku.delivery_store_id"
            + "   AND store.deleted_at IS NULL"
            + " LEFT JOIN t_delivery_detail detail "
            + "   ON store.delivery_detail_id = detail.id "
            + "   AND detail.deleted_at IS NULL "
            + " LEFT JOIN m_tnpmst tnp "
            + "   ON tnp.shpcd = store.store_code "
            + "   AND tnp.deleted_at IS NULL "
            + " WHERE 1=1 "
            + "   AND detail.delivery_id = :deliveryId "
            + "   AND store.store_code = :storeCode "
            + " ORDER BY "
            + "   ssku.color_code"
            + " , ssku.size", nativeQuery = true)
    List<ExtendedTDeliveryStoreSkuPDFEntity> findByDeliveryIdAndStoreCode(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("storeCode") String storeCode
            );

}
