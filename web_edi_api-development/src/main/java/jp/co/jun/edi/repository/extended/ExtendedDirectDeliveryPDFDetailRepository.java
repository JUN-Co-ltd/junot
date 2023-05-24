package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedDirectDeliveryPDFDetailEntity;

/**
 * ExtendedDirectDeliveryPDFHeaderRepository.
 */
@Repository
public interface ExtendedDirectDeliveryPDFDetailRepository
extends JpaRepository<ExtendedDirectDeliveryPDFDetailEntity, BigInteger> {

    /**
     * 納品出荷伝票PDF_一覧情報作成用SQL.
     *
     * @param deliveryId 納品ID
     * @param shopCode 店舗コード
     * @return 納品出荷伝票PDF_一覧情報
     */
    @Query(value = " SELECT "
            + "    ssku.id "
            + "  , item.part_no "
            + "  , ssku.color_code "
            + "  , ssku.size "
            + "  , item.product_name "
            + "  , odr.retail_price "
            + "  , ssku.delivery_lot"
            + "  , IFNULL(ssku.delivery_lot, 0) * IFNULL(odr.retail_price, 0) AS retail_price_sub_total"
            + " FROM t_delivery_store_sku ssku "
            + " LEFT JOIN t_delivery_store store "
            + "   ON ssku.delivery_store_id = store.id "
            + "   AND store.deleted_at IS NULL "
            + " LEFT JOIN t_delivery_detail detail "
            + "   ON detail.id = store.delivery_detail_id "
            + "   AND detail.deleted_at IS NULL "
            + " LEFT JOIN t_delivery delivery "
            + "   ON delivery.id = detail.delivery_id "
            + "   AND delivery.deleted_at IS NULL "
            + " LEFT JOIN t_item item "
            + "   ON item.id = delivery.part_no_id "
            + "   AND item.deleted_at IS NULL "
            + " LEFT JOIN t_order odr "
            + "   ON odr.id = delivery.order_id "
            + "   AND odr.deleted_at IS NULL "
            + " LEFT JOIN m_tnpmst tnp "
            + "   ON tnp.shpcd = store.store_code "
            + "   AND tnp.deleted_at IS NULL "
            + " WHERE 1=1 "
            + "   AND detail.delivery_id = :deliveryId "
            + "   AND tnp.shpcd = :shopCode "
            + "   AND ssku.deleted_at IS NULL "
            + " ORDER BY "
            + "   ssku.id", nativeQuery = true)
    List<ExtendedDirectDeliveryPDFDetailEntity> findByDeliveryColumns(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("shopCode") String shopCode
            );

}
