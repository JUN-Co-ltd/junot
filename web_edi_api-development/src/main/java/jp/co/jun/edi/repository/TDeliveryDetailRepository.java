package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Date;
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

import jp.co.jun.edi.entity.TDeliveryDetailEntity;

/**
 * TDeliveryDetailRequestRepository.
 */
@Repository
public interface TDeliveryDetailRepository extends JpaRepository<TDeliveryDetailEntity, BigInteger>, JpaSpecificationExecutor<TDeliveryDetailEntity> {
    /**
     * 納品IDから 納品明細情報 を検索する.
     *
     * @param deliveryId deliveryId
     * @param pageable pageable
     * @return 納品詳細情報を取得する
     */
    @Query("SELECT t FROM TDeliveryDetailEntity t"
            + " WHERE t.deliveryId = :deliveryId "
            + " AND t.deletedAt is null")
    Page<TDeliveryDetailEntity> findByDeliveryId(
            @Param("deliveryId") BigInteger deliveryId,
            Pageable pageable);

    /**
     * 発注Idから 納品明細情報 を検索する.
     *
     * @param orderId 発注No
     * @param pageable pageable
     * @return 納品明細情報
     */
    @Query("SELECT dd FROM TDeliveryDetailEntity dd"
            + " INNER JOIN TDeliveryEntity d"
            + " ON dd.deliveryId = d.id"
            + " WHERE d.orderId = :orderId"
            + " AND d.deletedAt is null"
            + " AND dd.deletedAt is null")
    Page<TDeliveryDetailEntity> findByOrderId(
            @Param("orderId") BigInteger orderId, Pageable pageable);

    /**
     * 納品ID,除外納品明細ID配列から納品明細情報を論理削除する.
     * 論理削除時に連携ステータスを LinkingStatusType.EXCLUDED を指定する.
     *
     * @param deliveryId 納品ID
     * @param excludeDeliveryDetailIds 除外納品明細ID配列
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_detail t"
            + " SET t.deleted_at = now(),"
            + " t.linking_status = 9,"
            + " t.updated_user_id = :updatedUserId,"
            + " t.updated_at = now()"
            + " WHERE t.delivery_id = :deliveryId"
            + " AND t.id NOT IN (:excludeDeliveryDetailIds)"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    int updateDetailDeletedAtByDeliveryIdAndExclusionIds(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("excludeDeliveryDetailIds") List<BigInteger> excludeDeliveryDetailIds,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 発注Idに紐づくデータから納品依頼回数の最大値を検索する.
     * @param orderId 発注No
     * @return 納品依頼回数の最大値
     */
    @Query("SELECT MAX(dd.deliveryCount) FROM TDeliveryDetailEntity dd"
            + " INNER JOIN TDeliveryEntity d"
            + " ON dd.deliveryId = d.id"
            + " WHERE d.orderId = :orderId")
    Integer findMaxDeliveryCount(
            @Param("orderId") BigInteger orderId);

    /**
     * 納品明細IDをキーに納品明細情報の採番処理を行う.
     *
     * @param deliveryNumber 納品No
     * @param deliveryRequestAt 納品依頼日
     * @param deliveryCount 納品依頼回数
     * @param deliveryRequestNumber 納品依頼No
     * @param junpcTanto 連携入力者
     * @param updatedUserId 更新ユーザID
     * @param deliveryDetailId 納品明細ID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_detail t"
            + " SET t.delivery_number = :deliveryNumber,"
            + " t.delivery_request_at = :deliveryRequestAt,"
            + " t.delivery_count = :deliveryCount,"
            + " t.delivery_request_number = :deliveryRequestNumber,"
            + " t.junpc_tanto = :junpcTanto,"
            + " t.updated_user_id = :updatedUserId,"
            + " t.updated_at = now()"
            + " WHERE t.id = :deliveryDetailId"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    int updateNumberingById(
            @Param("deliveryNumber") String deliveryNumber,
            @Param("deliveryRequestAt") Date deliveryRequestAt,
            @Param("deliveryCount") Integer deliveryCount,
            @Param("deliveryRequestNumber") String deliveryRequestNumber,
            @Param("junpcTanto") String junpcTanto,
            @Param("updatedUserId") BigInteger updatedUserId,
            @Param("deliveryDetailId") BigInteger deliveryDetailId);

    /**
     * 納品IDから納品明細情報を論理削除する.
     * 論理削除時に連携ステータスを LinkingStatusType.EXCLUDED を指定する.
     *
     * @param deliveryId 納品ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_detail t"
            + " SET t.deleted_at = now(),"
            + " t.linking_status = 9,"
            + " t.updated_user_id = :updatedUserId,"
            + " t.updated_at = now()"
            + " WHERE t.delivery_id = :deliveryId"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    int updateDetailDeletedAtByDeliveryId(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 納品IDをキーに納品明細共通項目を更新する.
     *
     * @param faxSend ファックス送信フラグ
     * @param deliveryAt 納期
     * @param deliveryId 納品ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_detail t"
            + " SET t.fax_send = :faxSend,"
            + " t.delivery_at = :deliveryAt,"
            + " t.correction_at = :deliveryAt,"
            + " t.updated_user_id = :updatedUserId,"
            + " t.updated_at = now()"
            + " WHERE t.delivery_id = :deliveryId"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    int updateDetailCommonItemsByDeliveryId(
            @Param("faxSend") boolean faxSend,
            @Param("deliveryAt") Date deliveryAt,
            @Param("deliveryId") BigInteger deliveryId,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * id配列で納品依頼明細情報を取得する.
     * 論理削除データも取得する.
     * @param ids id配列
     * @return 納品依頼明細情報
     */
    @Query("SELECT t FROM TDeliveryDetailEntity t"
            + " WHERE t.id IN (:ids)"
            + " AND t.deletedAt IS NULL")
    Optional<List<TDeliveryDetailEntity>> findByIds(@Param("ids") List<BigInteger> ids);

    /**
     * idで論理削除されていない納品依頼明細情報を取得する.
     * @param id id
     * @return 納品依頼明細情報
     */
    Optional<TDeliveryDetailEntity> findByIdAndDeletedAtIsNull(@Param("id") BigInteger id);

    /**
     * 納品ID、課コード、納品依頼回数をキーに納品明細情報を検索する.
     * @param deliveryId 納品ID
     * @param divisionCode 課コード
     * @param deliveryCount 納品依頼回数
     * @return 納品明細情報
     */
    @Query("SELECT t FROM TDeliveryDetailEntity t"
            + " WHERE t.deliveryId = :deliveryId"
            + "   AND t.divisionCode  = :divisionCode"
            + "   AND t.deliveryCount  = :deliveryCount"
            + "   AND t.deletedAt is null")
    Optional<TDeliveryDetailEntity> findByDivisionCodeAndDeliveryIdAndCount(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("divisionCode") String divisionCode,
            @Param("deliveryCount") int deliveryCount);

    /**
     * 配分出荷指示確定ファイルから更新対象の納品明細情報を検索.
     * @param orderNumber   発注No
     * @param deliveryCount 納品依頼回数
     * @param divisionCode 課コード
     * @return 納品明細情報
     */
    @Query(value = "SELECT detail.* FROM t_delivery_detail detail "
            + " INNER JOIN t_delivery td "
            + "   ON td.id = detail.delivery_id "
            + "   AND td.deleted_at  IS NULL "
            + " WHERE td.order_number = :orderNumber"
            + "   AND detail.delivery_count = :deliveryCount"
            + "   AND detail.division_code = :divisionCode"
            + "   AND detail.deleted_at IS NULL", nativeQuery = true)
    Optional<TDeliveryDetailEntity> findByOrderNumberAndDeliveryCountAndDivisionCode(
            @Param("orderNumber") BigInteger orderNumber,
            @Param("deliveryCount") int deliveryCount,
            @Param("divisionCode") String divisionCode);
}
