package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedDeliveryRequestPdfDetailEntity;

/**
 *
 * ExtendedDeliveryRequestHeaderRepository.
 *
 */
@Repository
public interface ExtendedDeliveryRequestDetailRepository extends JpaRepository<ExtendedDeliveryRequestPdfDetailEntity, BigInteger> {
    /**
     * 処理前の納品依頼メール送信の明細情報 を検索する.
     * @param deliveryId 納品ID
     * @param partNoKind 品種
     * @param pageable {@link Pageable}
     * @return 処理前の納品依頼メール送信の明細情報を取得する
     */
    @Query(value = " SELECT"
            + "   t2.id AS id"
            + "  ,t2.size AS size"
            + "  ,t2.color_code AS color_code"
            + "  ,( SELECT sub1.item2 AS item2 "
            + "     FROM m_codmst sub1 "
            + "     WHERE sub1.tblid = '10' "
            + "     AND sub1.code1 = t2.color_code "
            + "     AND sub1.deleted_at IS NULL "
            + "     AND sub1.mntflg IN ('1', '2', '') ) AS color_code_name"
            + "  ,SUM( CASE t2.division_code WHEN 11 THEN t2.delivery_lot ELSE 0 END ) AS division_code11" // 東京1課
            + "  ,SUM( CASE t2.division_code WHEN 12 THEN t2.delivery_lot ELSE 0 END ) AS division_code12" // 東京2課
            + "  ,SUM( CASE t2.division_code WHEN 13 THEN t2.delivery_lot ELSE 0 END ) AS division_code13" // 東京3課
            + "  ,SUM( CASE t2.division_code WHEN 14 THEN t2.delivery_lot ELSE 0 END ) AS division_code14" // 東京4課
            + "  ,SUM( CASE t2.division_code WHEN 15 THEN t2.delivery_lot ELSE 0 END ) AS division_code15" // 東京5課
            + "  ,SUM( CASE t2.division_code WHEN 16 THEN t2.delivery_lot ELSE 0 END ) AS division_code16" // 東京6課
            + "  ,SUM( CASE t2.division_code WHEN 17 THEN t2.delivery_lot ELSE 0 END ) AS division_code17" // 東京7課
            + "  ,SUM( CASE t2.division_code WHEN 21 THEN t2.delivery_lot ELSE 0 END ) AS division_code21" // 関西1課
            + "  ,SUM( CASE t2.division_code WHEN 22 THEN t2.delivery_lot ELSE 0 END ) AS division_code22" // 中国1課
            + "  ,SUM( CASE t2.division_code WHEN 18 THEN t2.delivery_lot ELSE 0 END ) AS division_code18" // 縫製検品
            + " FROM t_delivery_detail t1 "
            + " LEFT OUTER JOIN t_delivery_sku t2 "
            + "    ON t1.id = t2.delivery_detail_id "
            + "    AND t2.deleted_at IS NULL "
            + " LEFT JOIN m_sizmst t3"
            + "    ON t3.szkg = t2.size "
            + "    AND t3.deleted_at IS NULL "
            + "    AND t3.hscd = :partNoKind "
            + "    AND t3.mntflg IN ('1', '2', '')"
            + " WHERE"
            + "      t1.delivery_id = :deliveryId "
            + "  AND t1.deleted_at IS NULL "
            + " GROUP BY"
            + "   t2.size"
            + "  ,t2.color_code"
            + "  ,color_code_name"
            + " ORDER BY"
            + "   t2.color_code ASC"
            + "  ,t3.jun ASC", nativeQuery = true)
    Page<ExtendedDeliveryRequestPdfDetailEntity> findByDeliveryId(@Param("deliveryId") BigInteger deliveryId,
            @Param("partNoKind") String partNoKind, Pageable pageable);

}
