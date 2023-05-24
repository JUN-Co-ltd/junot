package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedWReplenishmentShippingInstructionEntity;

/**
 * 拡張補充出荷指示ワーク情報のrepository.
 */
@Repository
public interface ExtendedWReplenishmentShippingInstructionRepository
extends JpaRepository<ExtendedWReplenishmentShippingInstructionEntity, BigInteger> {

    /**
     * w_replenishment_shipping_instructionをベースに、拡張補充出荷指示ワーク一覧を生成.
     *
     * @return 拡張補充出荷指示ワーク情報のリスト
     */
    @Query(value = " SELECT "
            + "     rsi.manage_date "
            + "   , rsi.manage_at "
            + "   , rsi.sequence "
            + "   , CASE WHEN cargo_place IS NULL OR cargo_place IN ('',' ','  ') "
            + "     THEN '3' ELSE '4' END AS instructor_system "
            + "   , rsi.hold_shop_code AS origin_shop_code "
            + "   , '2' AS shipping_category "
            + "   , tnp.warekind AS cargo_place "
            + "   , tnp.bhinflg AS non_conforming_product_type "
            + "   , jun.hjun AS allocation_rank "
            + "   , rsi.shipment_shop_code AS shop_code "
            + "   , rsi.cargo_at "
            + "    , rsi.part_no "
            + "   , rsi.color_code "
            + "   , rsi.size "
            + "   , rsi.shipping_instruction_lot "
            + "   , 0 AS fix_shipping_instruction_lot "
            + "   , item.retail_price "
            + "   , tnp.rate "
            + "   , TRUNCATE(item.retail_price * (tnp.rate/100), 0) as wholesale_price "
            + "   , item.ps_type AS proper_sale_type "
            + "   , CASE WHEN item.ps_type = 'S' "
            + "     THEN item.sale_retail_price "
            + "     ELSE NULL "
            + "     END AS sale_retail_price "
            + "   , CASE WHEN item.ps_type = 'S' "
            + "     THEN ROUND((1-(item.sale_retail_price/item.retail_price)) * 100,1 ) "
            + "     ELSE NULL "
            + "     END AS off_percent "
            + "   , rsi.tanto AS instruction_manage_user_code "
            + "   , rsi.shipment_shop_code AS instruction_manage_shop_code "
            + "   , item.part_no AS instruction_manage_part_no "
            + "   , jun.hka AS instruction_manage_division_code "
            + " FROM "
            + "   w_replenishment_shipping_instruction rsi "
            + " INNER JOIN t_item item "
            + "   ON item.part_no = rsi.part_no "
            + "   AND item.deleted_at IS NULL "
            + " INNER JOIN ( "
            + "   SELECT tnp.shpcd, bs.brand_cd, tnp.warekind, tnp.bhinflg, 100 AS rate FROM m_tnpmst tnp "
            + "   INNER JOIN m_brand_shop bs ON bs.shop_cd = tnp.shpcd AND bs.deleted_at IS NULL "
            + "   WHERE 1=1 AND tnp.trkei = 2 AND tnp.deleted_at IS NULL "
            + "   UNION "
            + "   SELECT tnp.shpcd, bs.brand_cd, tnp.warekind, tnp.bhinflg, bs.proper_rate AS rate  FROM m_tnpmst tnp "
            + "   INNER JOIN m_brand_shop bs ON bs.shop_cd = tnp.shpcd AND bs.deleted_at IS NULL "
            + "   WHERE 1=1 AND tnp.trkei <> 2 AND tnp.deleted_at IS NULL "
            + " ) tnp "
            + "   ON  tnp.shpcd = rsi.hold_shop_code "
            + "   AND tnp.brand_cd = item.brand_code "
            + " INNER JOIN m_junmst jun "
            + "   ON  jun.brand = item.brand_code "
            + "   AND jun.shpcd = rsi.shipment_shop_code "
            + "   AND jun.deleted_at IS NULL ", nativeQuery = true)
            List<ExtendedWReplenishmentShippingInstructionEntity> findAllFromWReplenishmentShippingInstruction();
}
