package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TInventoryShipmentEntity;

/**
 * 在庫出荷指示情報のrepository.
 */
@Repository
public interface TInventoryShipmentRepository extends JpaRepository<TInventoryShipmentEntity, BigInteger> {

    /**
     * 出荷日、出荷場所、指示元システム、課コード、品番 をキーに在庫出荷指示情報を検索する.
     * @param cargoAt 出荷日
     * @param cargoPlace 出荷場所
     * @param instructorSystem 指示元システム
     * @param divisionCode 課コード
     * @param partNo 品番
     * @return 在庫出荷指示情報
     */
    @Query(value = " SELECT tis.* "
            + " FROM t_inventory_shipment tis "
            + " INNER JOIN m_junmst jun "
            + "   ON  SUBSTRING(tis.part_no,1,2) = jun.brand "
            + "   AND jun.shpcd = IFNULL((SELECT cm2.code2 FROM m_codmst cm2 "
            + "                            WHERE cm2.tblId = '49' "
            + "                              AND TRIM(cm2.item1) = tis.shop_code "
            + "                              AND cm2.code1 = CONCAT('0',tis.instructor_system)"
            + "                              AND cm2.mntflg IN ('1', '2', '')"
            + "                              AND cm2.deleted_at IS NULL"
            + "                          ) , tis.shop_code)"
            + "   AND jun.deleted_at IS NULL "
            + " WHERE 1=1 "
            + "   AND tis.deleted_at IS NULL "
            + "   AND tis.cargo_at = :cargoAt "
            + "   AND tis.cargo_place = :cargoPlace "
            + "   AND tis.instructor_system = :instructorSystem "
            + "   AND jun.hka   = :divisionCode "
            + "   AND tis.part_no = :partNo "
            + " ORDER BY "
            + "   tis.cargo_at "
            + " , tis.instructor_system "
            + " , jun.hka "
            + " , tis.part_no ", nativeQuery = true)
    List<TInventoryShipmentEntity> findByCargoAtAndInstructorSystemAndDivisionCodeAndPartNo(
            @Param("cargoAt") Date cargoAt,
            @Param("cargoPlace") String cargoPlace,
            @Param("instructorSystem") Integer instructorSystem,
            @Param("divisionCode") String divisionCode,
            @Param("partNo") String partNo);

    /**
     * 変更対象の店舗コードを取得.
     * @param wmsLinkingFileId wmsLinkingFileId
     * @return 在庫出荷指示情報リスト
     */
    @Query(value = " SELECT tins.* "
            + "      FROM t_inventory_shipment tins "
            + "      INNER JOIN t_wms_linking_file wlf "
            + "          ON tins.wms_linking_file_id =  wlf.id "
            + "          AND wlf.deleted_at IS NULL "
            + "      WHERE wlf.business_type = 'SZ' "
            + "          AND wlf.wms_linking_status = '02' "
            + "          AND tins.wms_linking_file_id =  :wmsLinkingFileId "
            + "          AND tins.instructor_system "
            + "             IN (0, 1, 2) "
            + "          AND tins.deleted_at IS NULL ", nativeQuery = true)
    List<TInventoryShipmentEntity> findByManageNumberAndWmsLinkingFileId(
            @Param("wmsLinkingFileId") BigInteger wmsLinkingFileId
            );

    /**
     * 在庫出荷確定確定ファイルから更新対象のt_inventory_shipment情報を検索.
     *
     * @param partNo 品番
     * @param colorCode カラーコード
     * @param size サイズ
     * @param shopCode 店舗コード
     * @param instructionManageNumber 指示明細_指示番号
     * @param instructionManageNumberLine 指示明細_指示番号行
     * @return 在庫出荷指示情報
     */
    @Query(value = "SELECT t.*"
            + "     FROM t_inventory_shipment t"
            + "     WHERE t.part_no = :partNo"
            + "         AND t.color_code = :colorCode"
            + "         AND t.size = :size"
            + "         AND t.shop_code = :shopCode"
            + "         AND t.instruction_manage_number = :instructionManageNumber"
            + "         AND t.instruction_manage_number_line = :instructionManageNumberLine"
            + "         AND t.shipment_voucher_number IS NULL"
            + "         AND t.deleted_at IS NULL", nativeQuery = true)
    Optional<TInventoryShipmentEntity> findByManageColumnAndSequence(
            @Param("partNo") String partNo,
            @Param("colorCode") String colorCode,
            @Param("size") String size,
            @Param("shopCode") String shopCode,
            @Param("instructionManageNumber") String instructionManageNumber,
            @Param("instructionManageNumberLine") Integer instructionManageNumberLine
            );
}
