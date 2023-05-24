package jp.co.jun.edi.repository.csv;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.csv.DistributionShipmentConfirmCsvFileEntity;

/**
 * DistributionShipmentConfirmCsvFileRepository.
 * 直送配分出荷確定ファイル作成バッチで使用するRepository.
 */
@Repository
public interface DistributionShipmentConfirmCsvFileRepository extends JpaRepository<DistributionShipmentConfirmCsvFileEntity, BigInteger> {

      /**
     * 倉庫連携ファイルIDをキーに直送配分出荷情報を検索する.
     * @param wmsLinkingFileId 倉庫連携ファイルID
     * @return 拡張直送配分出荷情報リスト
     */
    @Query(value = " SELECT dss.id "
            + "         ,   dss.manage_date "
            + "         ,   dss.manage_at "
            + "         ,   dss.manage_number "
            + "         ,   dss.line_number "
            + "         ,   dd.arrival_place "
            + "         ,   mtnp.distrikind "
            + "         ,   mtnp.shopkind "
            + "         ,   mtnp.shopfmt"
            + "         ,   ds.store_code "
            + "         ,   dss.shipment_voucher_number "
            + "         ,   dss.shipment_voucher_line "
            + "         ,   d.part_no "
            + "         ,   dss.color_code "
            + "         ,   dss.size "
            + "         ,   dss.delivery_lot "
            + "         ,   dss.arrival_lot "
            + "         ,   d.order_number "
            + "         ,   d.delivery_count "
            + "         ,   dd.division_code "
            + "     FROM t_delivery_store_sku dss "
            + "         INNER JOIN t_wms_linking_file w "
            + "             ON dss.wms_linking_file_id = w.id "
            + "         INNER JOIN t_delivery_store ds "
            + "             ON ds.id = dss.delivery_store_id "
            + "         INNER JOIN t_delivery_detail dd "
            + "             ON dd.id = ds.delivery_detail_id "
            + "         INNER JOIN t_delivery d "
            + "             ON d.id = dd.delivery_id "
            + "         INNER JOIN m_tnpmst mtnp "
            + "             ON mtnp.shpcd = ds.store_code "
            + "             AND mtnp.deleted_at IS NULL "
            + "     WHERE dss.wms_linking_file_id = :wmsLinkingFileId "
            + "         AND dss.deleted_at IS NULL ", nativeQuery = true)
    List<DistributionShipmentConfirmCsvFileEntity> findByWmsLinkingFileId(
            @Param("wmsLinkingFileId") BigInteger wmsLinkingFileId);
}
