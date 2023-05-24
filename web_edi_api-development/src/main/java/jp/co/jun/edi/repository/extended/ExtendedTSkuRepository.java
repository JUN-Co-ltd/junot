package jp.co.jun.edi.repository.extended;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTSkuEntity;

/**
 *
 * ExtendedTSkuRepository.
 *
 */
@Repository
public interface ExtendedTSkuRepository extends JpaRepository<ExtendedTSkuEntity, BigInteger> {

    /**
     * 品番IDから SKU情報+コード名称 を検索する.
     *
     * @param partNoId
     *            品番ID
     * @param pageable pageable
     * @return 拡張SKU情報を取得する
     */
    @Query(value = "SELECT t.* "
            + "   ,m1.item2 as color_name"
            + "   ,'' as jan_name"
            + " FROM t_sku t"
            + "   LEFT JOIN m_codmst m1 "
            + "          ON m1.tblid = '10' "
            + "         AND m1.mntflg != '3' "
            + "         AND m1.deleted_at is null "
            + "         AND t.color_code = m1.code1 "
            + " WHERE t.part_no_id = :partNoId "
            + " AND t.deleted_at is null ", nativeQuery = true)
    Page<ExtendedTSkuEntity> findByPartNoId(
            @Param("partNoId") BigInteger partNoId, Pageable pageable);

    /**
     * 納品IDに紐づく納品数量合計を取得する.
     *
     * @param deliveryId 納品Id
     * @return 納品数量合計
     */
    @Query(value = "SELECT SUM(delivery_lot)"
            + " FROM t_delivery_sku"
            + " WHERE delivery_detail_id IN"
            + "   ("
            + "     SELECT id"
            + "     FROM t_delivery_detail"
            + "     WHERE delivery_id = :deliveryId"
            + "     AND deleted_at is null "
            + "   ) "
            + " AND deleted_at is null ", nativeQuery = true)
    BigDecimal cntAllDeliveredLot(
            @Param("deliveryId") BigInteger deliveryId);
}
