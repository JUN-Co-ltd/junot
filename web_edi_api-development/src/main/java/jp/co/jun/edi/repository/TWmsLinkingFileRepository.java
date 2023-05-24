package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.WmsLinkingStatusType;

/**
 * 倉庫連携ファイル情報のリポジトリ.
 */
public interface TWmsLinkingFileRepository extends JpaRepository<TWmsLinkingFileEntity, BigInteger> {

    /**
     * 業務区分とWMS連携ステータスを基に倉庫連携ファイル情報を取得する.
     *
     * @param businessType 業務区分
     * @param wmsLinkingStatus WMS連携ステータス
     * @param pageable Pageable
     * @return 倉庫連携管理情報
     */
    @Query("SELECT t FROM TWmsLinkingFileEntity t"
            + " WHERE t.businessType = :businessType"
            + " AND t.wmsLinkingStatus = :wmsLinkingStatus"
            + " AND t.deletedAt is null")
    Page<TWmsLinkingFileEntity> findByBusinessTypeAndWmsLinkingStatusType(
            @Param("businessType") BusinessType businessType,
            @Param("wmsLinkingStatus") WmsLinkingStatusType wmsLinkingStatus,
            Pageable pageable);

    /**
     * IDを基にWMS連携ステータスを更新する.
     *
     * @param wmsLinkingStatus WMS連携ステータス
     * @param id 倉庫連携ファイルID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE TWmsLinkingFileEntity t"
            + " SET wmsLinkingStatus = :wmsLinkingStatus ,"
            + " t.updatedUserId = :updatedUserId ,"
            + " t.updatedAt = now()"
            + " WHERE t.id = :id"
            + " AND t.deletedAt is null")
    int updateWmsLinkingStatus(
            @Param("wmsLinkingStatus") WmsLinkingStatusType wmsLinkingStatus,
            @Param("id") BigInteger id,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * IDリストを基にWMS連携ステータスを更新する.
     *
     * @param wmsLinkingStatus WMS連携ステータス
     * @param ids 倉庫連携ファイルIDリスト
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE TWmsLinkingFileEntity t"
            + " SET wmsLinkingStatus = :wmsLinkingStatus ,"
            + " t.updatedUserId = :updatedUserId ,"
            + " t.updatedAt = now()"
            + " WHERE t.id IN (:ids)"
            + " AND t.deletedAt is null")
    int updateWmsLinkingStatusByIds(
            @Param("wmsLinkingStatus") WmsLinkingStatusType wmsLinkingStatus,
            @Param("ids") List<BigInteger> ids,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 仕入情報にに紐づく送信済倉庫連携ファイル情報の件数.
     * @param orderId 発注No
     * @param purchaseCount 引取回数
     * @return ファイル作成済倉庫連携ファイル情報の件数
     */
    @Query(value = " SELECT COUNT(distinct w.id) "
            + "      FROM t_wms_linking_file w "
            + "         INNER JOIN t_purchase p "
            + "             ON p.wms_linking_file_id = w.id "
            + "         AND w.wms_linking_status = '02' "
            + "      WHERE p.order_id = :orderId "
            + "         AND p.purchase_count =  :purchaseCount ", nativeQuery = true)
    int countByPurchaseFileCreating(
            @Param("orderId") BigInteger orderId,
            @Param("purchaseCount") Integer purchaseCount);

}
