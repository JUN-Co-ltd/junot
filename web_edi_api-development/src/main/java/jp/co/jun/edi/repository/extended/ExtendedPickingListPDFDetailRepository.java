package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedPickingListPDFDetailEntity;

/**
 * ExtendedDirectDeliveryPDFHeaderRepository.
 */
@Repository
public interface ExtendedPickingListPDFDetailRepository
extends JpaRepository<ExtendedPickingListPDFDetailEntity, BigInteger> {

    /**
     * ピッキングリストPDF詳細部作成用SQL.
     *
     * @param deliveryId 納品ID
     * @param divisionCode 課コード
     * @return PDF詳細部情報
     */
    @Query(value = " SELECT "
            + "     store.id "
            + "   , tnp.shpcd "
            + "   , tnp.name "
            + "   , detail.allocation_cargo_at "
            + "   , detail.allocation_type "
            + " FROM t_delivery_store store "
            + " LEFT JOIN m_tnpmst tnp "
            + "   ON tnp.shpcd = store.store_code "
            + "   AND tnp.deleted_at IS NULL "
            + " LEFT JOIN t_delivery_detail detail "
            + "   ON detail.id = store.delivery_detail_id "
            + "   AND detail.deleted_at IS NULL "
            + "  WHERE 1=1 "
            + "    AND detail.delivery_id = :deliveryId "
            + "    AND detail.division_code = :divisionCode "
            + "    AND store.deleted_at IS NULL "
            + "  ORDER BY "
            + "    store.store_code"
            + "  , store.id", nativeQuery = true)
    List<ExtendedPickingListPDFDetailEntity> findByDeliveryIdAndDivisionCode(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("divisionCode") String divisionCode
            );

}
