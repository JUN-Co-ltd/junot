package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.VDelischeDeliverySkuEntity;
import jp.co.jun.edi.repository.constants.DelischeConstants;

/**
 * VDelischeDeliverySkuRepository.
 */
@Repository
public interface VDelischeDeliverySkuRepository extends JpaRepository<VDelischeDeliverySkuEntity, BigInteger>,
JpaSpecificationExecutor<VDelischeDeliverySkuEntity> {

    /**
     * デリスケ納品依頼レコード配下のデリスケ納品SKUを取得する.
     *
     * @param orderId 発注Id
     * @param deliveryId 納品Id
     * @param pageable pageable
     * @return デリスケ納品SKU
     */
    @Query(value = "SELECT ds.id as id"
            + "   , o.id as order_id"
            + "   , d.id as delivery_id"
            + "   , dd.id as delivery_detail_id"
            + "   , dd.correction_at as delivery_at"
            + "   , "
            + DelischeConstants.DELIVERY_AT_MONTHLY_SQL
            + "     as delivery_at_monthly"
            + "   , i.brand_code as brand_code"
            + "   , i.item_code as item_code"
            + "   , i.part_no as part_no"
            + "   , i.product_name as product_name"
            + "   , ds.color_code as color_code"
            + "   , ds.size as size"
            + "   , "
            + DelischeConstants.SEASON_SQL
            + "     as season"
            + "   , o.mdf_maker_code as mdf_maker_code"
            + "   , srm.name as mdf_maker_name"
            + "   , o.product_order_at as product_order_at"
            + "   , o.product_delivery_at as product_delivery_at"
            + "   , "
            + DelischeConstants.LATE_DELIVERY_AT_FLG_SQL
            + "     as late_delivery_at_flg"
            + "   , os.product_order_lot as product_order_lot"
            + "   , SUM(ds.delivery_lot) as delivery_lot"
            + "   , SUM(ds.arrival_lot) as arrival_lot"
            + "   , o.retail_price as retail_price"
            + "   , o.product_cost as product_cost"
            + "   , CAST(szm.jun as SIGNED) as jun"
            + "   , o.order_approve_status as order_approve_status"
            + "   , i.quality_composition_status as quality_composition_status"
            + "   , i.quality_coo_status as quality_coo_status"
            + "   , i.quality_harmful_status as quality_harmful_status"
            + " FROM t_order o"
            + "   INNER JOIN t_delivery d"
            + "           ON o.id = d.order_id"
            + "          AND d.id = :deliveryId"
            + "          AND d.deleted_at IS NULL"
            + "   INNER JOIN t_delivery_detail dd"
            + "           ON d.id = dd.delivery_id"
            + "          AND dd.deleted_at IS NULL"
            + "   INNER JOIN t_delivery_sku ds"
            + "           ON dd.id = ds.delivery_detail_id"
            + "          AND ds.deleted_at IS NULL"
            + "   INNER JOIN t_order_sku os"
            + "           ON d.order_id = os.order_id"
            + "          AND os.deleted_at IS NULL"
            + "          AND ds.color_code = os.color_code"
            + "          AND ds.size = os.size"
            + "   INNER JOIN t_item i"
            + "           ON o.part_no_id = i.id"
            + "          AND i.deleted_at IS NULL"
            + "   LEFT OUTER JOIN m_sirmst srm"
            + "           ON o.mdf_maker_code = srm.sire"
            + "          AND srm.deleted_at IS NULL"
            + "          AND srm.mntflg IN ('1', '2', '')"
            + "   LEFT OUTER JOIN m_sizmst szm"
            + "           ON szm.hscd = CONCAT(i.brand_code, i.item_code)"
            + "          AND szm.szkg =  ds.size"
            + "          AND szm.deleted_at IS NULL"
            + "          AND szm.mntflg IN ('1', '2', '')"
            + " WHERE o.id = :orderId"
            + "   AND o.deleted_at IS NULL"
            + " GROUP BY o.id"
            + "        , d.id"
            + "        , ds.color_code"
            + "        , ds.size"
            + " ORDER BY ds.color_code ASC, jun ASC", nativeQuery = true)
    Page<VDelischeDeliverySkuEntity> findByDelischeDeriveryRequest(
            @Param("orderId") BigInteger orderId,
            @Param("deliveryId") BigInteger deliveryId,
            Pageable pageable);
}
