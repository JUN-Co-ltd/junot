package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.schedule.ExtendedInventoryShipmentScheduleEntity;

/**
 * ExtendedInventoryShipmentScheduleRepository.
 * 在庫出荷ファイル作成バッチで使用する在庫出荷指示情報Repository.
 */
@Repository
public interface ExtendedInventoryShipmentScheduleRepository extends JpaRepository<ExtendedInventoryShipmentScheduleEntity, BigInteger> {

    /**
     * 倉庫連携ファイルIDをキーに在庫出荷指示情報を検索する.
     * @param wmsLinkingFileId 倉庫連携ファイルID
     * @return 拡張在庫出荷指示情報リスト
     */
    @Query(value = " SELECT ins.*"
            + "      FROM t_inventory_shipment ins "
            + "      WHERE ins.wms_linking_file_id = :wmsLinkingFileId "
            + "         AND ins.lg_send_type = '1'"
            + "         AND ins.deleted_at IS NULL", nativeQuery = true)
    List<ExtendedInventoryShipmentScheduleEntity> findByWmsLinkingFileId(@Param("wmsLinkingFileId") BigInteger wmsLinkingFileId);
}
