package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.schedule.ExtendedDistributionShipmentScheduleEntity;

/**
 * ExtendedDistributionShipmentScheduleRepository.
 * 配分出荷指示ファイル作成バッチで使用する配分出荷指示ファイル情報Repository.
 */
@Repository
public interface ExtendedDistributionShipmentScheduleRepository extends JpaRepository<ExtendedDistributionShipmentScheduleEntity, BigInteger> {

    /**
     * 倉庫連携ファイルIDをキーに配分出荷指示情報を検索する.
     * @param wmsLinkingFileId 倉庫連携ファイルID
     * @return 配分出荷指示情報リスト
     */
    @Query(value = " SELECT "
           + "     dss.id "
           + "    ,dss.manage_date "
           + "    ,dss.manage_at "
           + "    ,dss.manage_number "
           + "    ,dss.line_number "
           + "    , (CASE WHEN td.non_conforming_product_type = 1 "
           + "       THEN CONCAT(detail.arrival_place,'B') "
           + "       ELSE detail.arrival_place "
           + "       END) AS arrival_place "
           + "    ,mtnp2.hjun "
           // PRD_0082(再修正) mod SIT start
           //+ "    ,mtnp.suspend_type "
           + "    , (CASE WHEN mtnp.distrikind = 0 "
           + "       THEN 0 ELSE 1 END ) as suspend_type "
           // PRD_0082(再修正) mod SIT end
           + "    ,store.store_code "
           + "    ,td.part_no "
           + "    ,dss.color_code "
           + "    ,dss.size "
           + "    ,dss.delivery_lot "
           + "    ,item.retail_price "
           + "    ,mtnp2.percent "
           + "    ,item.ps_type "
           + "    , TRUNCATE(item.retail_price * (mtnp2.percent/100), 0) as under_retail_price"
           + "    , (CASE WHEN item.ps_type = 'S' "
           + "       THEN item.sale_retail_price "
           + "       ELSE '' "
           + "       END) AS sale_retail_price "
           + "    , (CASE WHEN item.ps_type = 'S' "
           + "       THEN ROUND((1-(item.sale_retail_price /item.retail_price))*100, 1) "
           + "       ELSE '' "
           + "       END) AS off_percent "
           + "    ,td.order_number "
           + "    ,td.delivery_count "
           + "    ,detail.division_code "
           + " FROM t_delivery_store_sku dss "
           + " INNER JOIN t_delivery_store store "
           + " ON dss.delivery_store_id = store.id "
           + " AND store.deleted_at IS NULL "
           + " INNER JOIN t_delivery_detail detail "
           + " ON store.delivery_detail_id = detail.id "
           + " AND detail.deleted_at IS NULL "
           + " INNER JOIN t_delivery td "
           + " ON td.id = detail.delivery_id "
           + " AND td.deleted_at IS NULL "
           + " INNER JOIN t_item item "
           + " ON item.id = td.part_no_id "
           + " AND item.deleted_at IS NULL "
           // PRD_0082(再修正) mod SIT start
           //+ " INNER JOIN ( "
           //+ "      SELECT mtnp.shpcd, mtnp.deleted_at "
           //+ "         ,0 AS suspend_type "
           //+ "      FROM m_tnpmst mtnp "
           //// PRD_0082 mod SIT start
           ////+ "      WHERE 1=1 AND mtnp.distrikind = 1 AND mtnp.shopkind = 130 AND mtnp.shopfmt = 1 "
           //+ "      WHERE 1=1 AND mtnp.distrikind = 1 AND mtnp.shopkind = 1 AND mtnp.shopfmt = 130 "
           //// PRD_0082 mod SIT end
           //+ "      UNION "
           //+ "      SELECT mtnp.shpcd, mtnp.deleted_at "
           //+ "         ,CASE WHEN mtnp.distrikind <> 0 THEN 1 ELSE 0 END AS suspend_type "
           //+ "      FROM m_tnpmst mtnp "
           //// PRD_0082 mod SIT start
           ////+ "      WHERE 1=1 AND mtnp.distrikind <> 1 AND mtnp.shopkind <> 130 AND mtnp.shopfmt <> 1 "
           //+ "      WHERE 1=1 AND mtnp.distrikind <> 1 AND mtnp.shopkind <> 1 AND mtnp.shopfmt <> 130 "
           //// PRD_0082 mod SIT end
           //+ "  ) mtnp "
           + " INNER JOIN m_tnpmst mtnp"
           // PRD_0082(再修正) mod SIT end
           + " ON mtnp.shpcd = store.store_code "
           + " AND mtnp.deleted_at IS NULL "
           + " INNER JOIN ( "
           + "    SELECT mjun.hjun, mjun.brand, inner_mtnp.shpcd, inner_mtnp.deleted_at, 100 as percent FROM m_tnpmst inner_mtnp "
           // PRD_0082 mod SIT start
           //+ "    INNER JOIN m_junmst mjun ON mjun.shpcd = inner_mtnp.shpcd AND mjun.deleted_at IS NULL "
           + "    INNER JOIN m_junmst mjun ON mjun.shpcd = inner_mtnp.shpcd AND mjun.deleted_at IS NULL AND mjun.mntflg <> '3' "
           // PRD_0082 mod SIT end
           + "    WHERE inner_mtnp.shopkind = '2' "
           + "    UNION "
           + "    SELECT mjun.hjun, mjun.brand, inner_mtnp.shpcd, inner_mtnp.deleted_at, bs.proper_rate as percent "
           + "    FROM m_tnpmst inner_mtnp "
           // PRD_0082 mod SIT start
           //+ "    INNER JOIN m_junmst mjun ON mjun.shpcd = inner_mtnp.shpcd AND mjun.deleted_at IS NULL "
           //+ "    INNER JOIN m_brand_shop bs ON bs.brand_cd = mjun.brand AND bs.shop_cd = inner_mtnp.shpcd "
           + "    INNER JOIN m_junmst mjun ON mjun.shpcd = inner_mtnp.shpcd AND mjun.deleted_at IS NULL AND mjun.mntflg <> '3' "
           + "    INNER JOIN m_brand_shop bs ON bs.brand_cd = mjun.brand AND bs.shop_cd = inner_mtnp.shpcd AND bs.deleted_at IS NULL "
           // PRD_0082 mod SIT end
           + "    WHERE inner_mtnp.shopkind != '2' "
           + " ) mtnp2 "
           + " ON mtnp2.shpcd = store.store_code "
           + " AND mtnp2.brand = item.brand_code "
           + " AND mtnp2.deleted_at IS NULL "
           + " INNER JOIN t_order odr "
           + " ON td.order_id = odr.id "
           + " AND odr.deleted_at IS NULL "
           + " WHERE dss.wms_linking_file_id = :wmsLinkingFileId "
           // PRD_0082 add SIT start
           + " AND dss.deleted_at IS NULL "
           // PRD_0082 add SIT end
           + " ORDER BY dss.line_number ASC", nativeQuery = true)
    List<ExtendedDistributionShipmentScheduleEntity> findByWmsLinkingFileId(@Param("wmsLinkingFileId") BigInteger wmsLinkingFileId);
}
