package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.type.BooleanType;

/**
 * TDeliveryRequestRepository.
 */
@Repository
public interface TDeliveryRepository extends JpaRepository<TDeliveryEntity, BigInteger>, JpaSpecificationExecutor<TDeliveryEntity> {

    /**
     * 発注Noから 納品情報 を検索する.
     *
     * @param orderNumber 発注No
     * @param pageable pageable
     * @return 納品情報
     */
    @Query("SELECT t FROM TDeliveryEntity t"
            + " WHERE t.orderNumber = :orderNumber"
            + " AND t.deletedAt is null")
    Page<TDeliveryEntity> findByOrderNumber(
            @Param("orderNumber") BigInteger orderNumber, Pageable pageable);

    /**
     * 発注Idから 納品情報 を検索する.
     *
     * @param orderId 発注Id
     * @param pageable pageable
     * @return 納品情報
     */
    @Query("SELECT t FROM TDeliveryEntity t"
            + " WHERE t.orderId = :orderId"
            + " AND t.deletedAt is null")
    Page<TDeliveryEntity> findByOrderId(
            @Param("orderId") BigInteger orderId, Pageable pageable);

    /**
     * 発注Idに紐づく納品依頼情報の数を取得.
     *
     * @param orderId 発注Id
     * @return 取得件数
     */
    @Query("SELECT COUNT(t.id) FROM TDeliveryEntity t"
            + " WHERE t.orderId = :orderId"
            + " AND t.deletedAt is null")
    int countByOrderId(
            @Param("orderId") BigInteger orderId);

    /**
     * SQをロックする.
     *
     * @param booleanType booleanType
     * @param deliveryId 納品ID
     * @param sqLockUserId ユーザーId
     * @return 更新件数
     */
    @Modifying
    @Query("UPDATE TDeliveryEntity t"
            + " SET t.sqLockFlg = :booleanType"
            + "    ,t.sqLockUserId = :sqLockUserId"
            + "    ,t.updatedUserId = :sqLockUserId"
            + " WHERE t.id = :deliveryId"
            + " AND t.deletedAt is null")
    int lockSq(
            @Param("booleanType") BooleanType booleanType,
            @Param("deliveryId") BigInteger deliveryId,
            @Param("sqLockUserId") BigInteger sqLockUserId);

    /**
     * 発注IDと、承認ステータスが一致する納品依頼情報が存在するか.
     *
     * @param orderId 発注ID
     * @param deliveryApproveStatus 承認ステータス
     * @return 承認ステータスが合致する納品依頼情報リスト
     */
    @Query("SELECT COUNT(t.id) > 0  FROM TDeliveryEntity t"
            + " WHERE t.orderId = :orderId"
            + " AND t.deliveryApproveStatus = :deliveryApproveStatus"
            + " AND t.deletedAt is null")
    boolean existsByOrderIdAndDeliveryApproveStatus(
            @Param("orderId") BigInteger orderId,
            @Param("deliveryApproveStatus") String deliveryApproveStatus);

    /**
     * 品番Idから承認ステータスが合致する納品依頼情報リストを取得.
     * @param partNoId 品番Id
     * @param deliveryApproveStatus 承認ステータス
     * @return 承認ステータスが合致する納品依頼情報リスト
     */
    @Query("SELECT t FROM TDeliveryEntity t"
            + " WHERE t.partNoId = :partNoId"
            + " AND t.deliveryApproveStatus = :deliveryApproveStatus"
            + " AND t.deletedAt is null")
    List<TDeliveryEntity> findMatchApproveStatusDeliverysByPartNoId(
            @Param("partNoId") BigInteger partNoId,
            @Param("deliveryApproveStatus") String deliveryApproveStatus);

    /**
     * @param ids 納品ID
     * @return 納品情報リスト
     */
    @Query("SELECT t FROM TDeliveryEntity t"
            + " WHERE t.id IN :ids"
            + " AND t.deletedAt is null")
    Optional<List<TDeliveryEntity>> findByIds(
            @Param("ids") List<BigInteger> ids);

    /**
     * idで論理削除されていない納品依頼情報を取得する.
     * @param id id
     * @return 納品依頼情報
     */
    Optional<TDeliveryEntity> findByIdAndDeletedAtIsNull(@Param("id") BigInteger id);


    /**
     * 納品得意先SKUの倉庫連携IDをキーに、納品得意先SKUに紐付く納品情報を検索.
     * @param wmsLinkingFileId 倉庫連携ファイルID
     * @return 納品情報
     */
    @Query(value = " SELECT DISTINCT d.* "
            + "      FROM t_delivery d "
            + "         INNER JOIN t_delivery_detail dd "
            + "             ON d.id = dd.delivery_id "
            + "         INNER JOIN t_delivery_store ds "
            + "             ON dd.id = ds.delivery_detail_id "
            + "         INNER JOIN t_delivery_store_sku dss "
            + "             ON ds.id = dss.delivery_store_id "
            + "      WHERE dss.wms_linking_file_id= :wmsLinkingFileId ", nativeQuery = true)
    Optional<TDeliveryEntity> findByWmsLinkingFileId(
            @Param("wmsLinkingFileId") BigInteger wmsLinkingFileId);

}
