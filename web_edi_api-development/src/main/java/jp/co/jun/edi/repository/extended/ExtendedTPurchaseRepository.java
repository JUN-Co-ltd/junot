package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.schedule.ExtendedTPurchaseLinkingCreateCsvFileEntity;

/**
 * ExtendedTPurchaseRepository.
 * 仕入バッチで使用する拡張仕入情報Repository.
 */
@Repository
public interface ExtendedTPurchaseRepository extends JpaRepository<ExtendedTPurchaseLinkingCreateCsvFileEntity, BigInteger> {

    /**
     * 管理Noをキーに拡張仕入情報を検索する.
     * @param wmsLinkingFileId 倉庫連携ファイルID
     * @return 仕入情報リスト
     */
    @Query(value = "SELECT t.* "
            + "   ,o.id as check_order_id"
            + "   ,o.unit_price as unit_price"
            + "   ,o.retail_price as retail_price"
            + " FROM t_purchase t"
            + "   LEFT JOIN t_order o "
            + "          ON t.order_id = o.id "
            + "         AND o.deleted_at is null "
            + " WHERE t.wms_linking_file_id = :wmsLinkingFileId "
            + " AND t.deleted_at is null "
            + " ORDER BY t.sq_manage_number ASC", nativeQuery = true)
    List<ExtendedTPurchaseLinkingCreateCsvFileEntity> findByWmsLinkingFileId(@Param("wmsLinkingFileId") BigInteger wmsLinkingFileId);
}
