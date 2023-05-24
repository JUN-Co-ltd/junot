package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedPdfTDeliveryDetailEntity;

/**
 *
 * ExtendedTDeliveryRepository.
 *
 */
@Repository
public interface ExtendedPdfTDeliveryDetailRepository extends JpaRepository<ExtendedPdfTDeliveryDetailEntity, BigInteger> {
    /**
     * 納品詳細情報を取得する.
     * @param deliveryId 納品ID
     * @param pageable {@link Pageable}
     * @return 納品詳細情報
     */
    @Query(value = " SELECT"
            + "   t1.id"
            + "  ,t1.delivery_request_number"
            + "  ,t1.delivery_number"
            + "  ,t1.correction_at"
            + "  ,t1.logistics_code"
            + "  ,t1.division_code"
            + "  ,( SELECT sub1.item1 "
            + "     FROM m_codmst sub1 "
            + "     WHERE sub1.deleted_at IS NULL  "
            + "     AND sub1.tblid= '21'  "
            + "     AND sub1.mntflg IN ('1', '2', '') "
            + "     AND sub1.code1 = t1.logistics_code ) AS delivery_location"
            + "  ,t3.fax"
            + "  ,t3.company_name"
            + "  ,t3.postal_code"
            + "  ,t3.address"
            + "  ,t3.tel"
            + " FROM t_delivery_detail t1"
            + " LEFT JOIN m_delivery_destination t2 "
            + "   ON t2.deleted_at IS NULL"
            + "   AND  t1.logistics_code = t2.logistics_code"
            + " LEFT JOIN m_delivery_location t3"
            + "   ON t3.deleted_at IS NULL"
            + "   AND t2.delivery_location_id = t3.id"
            + " WHERE t1.deleted_at IS NULL"
            + " AND t1.delivery_id = :deliveryId", nativeQuery = true)
    Page<ExtendedPdfTDeliveryDetailEntity> findByDeliveryId(@Param("deliveryId") BigInteger deliveryId, Pageable pageable);
}
