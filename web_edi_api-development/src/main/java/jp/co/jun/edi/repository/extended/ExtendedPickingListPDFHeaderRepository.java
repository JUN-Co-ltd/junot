package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedPickingListPDFHeaderEntity;

/**
 * ExtendedDirectDeliveryPDFHeaderRepository.
 */
@Repository
public interface ExtendedPickingListPDFHeaderRepository
extends JpaRepository<ExtendedPickingListPDFHeaderEntity, BigInteger> {

    /**
     * ピッキングリストPDFヘッダ作成用SQL.
     * ※ ブランド名・課名は別メソッド経由で取得
     *
     * @param deliveryId 納品ID
     * @return PDFヘッダ情報
     */
    @Query(value = " SELECT "
            + "     store.id "
            + "   , item.brand_code "
            + "   , mjun.hka AS division_code "
            + "   , item.part_no "
            + "   , item.product_name "
            + "   , delivery.order_number "
            + "   , delivery.delivery_count "
            + "   , delivery.non_conforming_product_type "
            + "   , delivery.non_conforming_product_unit_price "
            + "   , odr.retail_price "
            + "   , detail.correction_at "
            + "   , detail.allocation_complete_payment_flg "
            + "   , detail.arrival_at "
            + "   , msir.sire "
            + "   , msir.name "
            + " FROM t_delivery_store store "
            + " LEFT JOIN t_delivery_detail detail "
            + "   ON store.delivery_detail_id = detail.id "
            + "   AND detail.deleted_at IS NULL "
            + " LEFT JOIN t_delivery delivery "
            + "   ON detail.delivery_id = delivery.id "
            + "   AND delivery.deleted_at IS NULL "
            + " LEFT JOIN t_order odr "
            + "   ON delivery.order_id = odr.id "
            + "   AND odr.deleted_at IS NULL "
            + " LEFT JOIN t_item item "
            + "   ON item.id = delivery.part_no_id "
            + "   AND item.deleted_at IS NULL "
            + " LEFT JOIN m_junmst mjun "
            + "   ON mjun.shpcd = store.store_code "
            + "   AND mjun.brand = item.brand_code "
            + "   AND mjun.deleted_at IS NULL "
            + " LEFT JOIN m_sirmst msir "
            + "   ON msir.sire = odr.mdf_maker_code "
            + "   AND msir.deleted_at IS NULL "
            + "  WHERE 1=1 "
            + "    AND detail.delivery_id = :deliveryId "
            + "  GROUP BY "
            + "    mjun.hka "
            + "  ORDER BY "
            + "    store.id", nativeQuery = true)
    List<ExtendedPickingListPDFHeaderEntity> findByDeliveryId(
            @Param("deliveryId") BigInteger deliveryId
            );
}
