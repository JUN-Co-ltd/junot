package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedDirectDeliveryPDFHeaderEntity;

/**
 * ExtendedDirectDeliveryPDFHeaderRepository.
 */
@Repository
public interface ExtendedDirectDeliveryPDFHeaderRepository
extends JpaRepository<ExtendedDirectDeliveryPDFHeaderEntity, BigInteger> {
    /**
     * 納品出荷伝票PDF_ヘッダ情報作成用SQL.
     *
     * @param deliveryId 納品ID
     * @return 納品出荷伝票PDF_ヘッダ情報
     */
    @Query(value = " SELECT "
            + "   store.id AS id "
            + " , tnp.yubin AS yubin "
            + " , tnp.add1 AS address1 "
            + " , tnp.add2 AS address2 "
            + " , tnp.add3 AS address3 "
            + " , tnp.shpcd AS shop_code "
            + " , tnp.name AS shop_name "
            + " FROM "
            + "   t_delivery_store store "
            + " LEFT JOIN t_delivery_detail detail "
            + "   ON detail.id = store.delivery_detail_id "
            + "   AND detail.deleted_at IS NULL "
            + " LEFT JOIN m_tnpmst tnp "
            + "   ON tnp.shpcd = store.store_code "
            + "   AND tnp.deleted_at IS NULL "
            + " WHERE 1=1 "
            + "   AND detail.delivery_id = :deliveryId "
            + "   AND store.deleted_at IS NULL "
            + " ORDER BY "
            + "   tnp.shpcd "
            + " , store.id", nativeQuery = true)
    List<ExtendedDirectDeliveryPDFHeaderEntity> findByDeliveryColumns(
            @Param("deliveryId") BigInteger deliveryId);
}
