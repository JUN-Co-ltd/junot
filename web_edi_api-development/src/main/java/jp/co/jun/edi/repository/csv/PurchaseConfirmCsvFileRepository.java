package jp.co.jun.edi.repository.csv;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.csv.PurchaseConfirmCsvFileEntity;

/**
 * PurchaseConfirmCsvFileRepository.
 * 直送仕入確定ファイル作成バッチで使用するRepository.
 */
@Repository
public interface PurchaseConfirmCsvFileRepository extends JpaRepository<PurchaseConfirmCsvFileEntity, BigInteger> {

    /**
     * 倉庫連携ファイルIDをキーに仕入情報を検索する.
     * @param wmsLinkingFileId 倉庫連携ファイルID
     * @return 拡張仕入情報リスト
     */
    @Query(value = " SELECT p.*"
            + "      FROM t_purchase p "
            + "      WHERE p.wms_linking_file_id = :wmsLinkingFileId "
            + "         AND p.lg_send_type = '1'"
            + "         AND p.deleted_at IS NULL", nativeQuery = true)
    List<PurchaseConfirmCsvFileEntity> findByWmsLinkingFileId(@Param("wmsLinkingFileId") BigInteger wmsLinkingFileId);
}
