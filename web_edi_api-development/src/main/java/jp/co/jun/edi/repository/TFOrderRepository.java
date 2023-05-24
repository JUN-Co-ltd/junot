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

import jp.co.jun.edi.entity.TFOrderEntity;
import jp.co.jun.edi.type.FukukitaruMasterConfirmStatusType;
import jp.co.jun.edi.type.FukukitaruMasterLinkingStatusType;

/**
 * フクキタル用発注情報を検索するリポジトリ.
 */
public interface TFOrderRepository extends JpaRepository<TFOrderEntity, BigInteger>, JpaSpecificationExecutor<TFOrderEntity> {
    /**
     * フクキタル発注Idから発注情報を取得.
     * @param fOrderId フクキタル発注Id
     * @return 発注情報
     */
    @Query("SELECT t FROM TFOrderEntity t"
            + " WHERE t.id = :fOrderId"
            + " AND t.deletedAt is null")
    Optional<TFOrderEntity> findByIdDeletedAtIsNull(
            @Param("fOrderId") BigInteger fOrderId);

    /**
     * オーダー識別コードを更新する.
     * @param id フクキタル発注ID
     * @param orderCode オーダー識別コード
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE TFOrderEntity t"
            + " SET orderCode = :orderCode"
            + " WHERE t.id = :id")
    int updateOrderCode(
            @Param("id") BigInteger id,
            @Param("orderCode") String orderCode);

    /**
     * 発注Idからフクキタル発注情報を取得.
     * @param orderId 発注Id
     * @param pageable Pageable
     * @return フクキタル発注情報
     */
    @Query("SELECT t FROM TFOrderEntity t"
            + " WHERE t.orderId = :orderId"
            + " AND t.deletedAt is null")
    Page<TFOrderEntity> findByOrderId(
            @Param("orderId") BigInteger orderId, Pageable pageable);

    /**
     * 品番Idからフクキタル発注情報を取得.
     * @param fItemId フクキタル品番ID
     * @param pageable Pageable
     * @return フクキタル発注情報
     */
    @Query("SELECT t FROM TFOrderEntity t"
            + " WHERE t.fItemId = :fItemId"
            + " AND t.deletedAt IS NULL")
    Page<TFOrderEntity> findByFItemId(
            @Param("fItemId") BigInteger fItemId, Pageable pageable);

    /**
     * 連携ステータスを更新する.
     * @param linkingStatus 連携ステータス
     * @param ids フクキタル発注IDリスト
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE TFOrderEntity t"
            + " SET linkingStatus = :linkingStatus ,"
            + " t.updatedUserId = :updatedUserId ,"
            + " t.updatedAt = now()"
            + " WHERE t.id IN (:ids) ")
    int updateLinkingStatus(
            @Param("linkingStatus") FukukitaruMasterLinkingStatusType linkingStatus,
            @Param("ids") List<BigInteger> ids,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 連携ステータスを更新する.
     * @param linkingStatus 連携ステータス
     * @param fOrderId フクキタル発注ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE TFOrderEntity t"
            + " SET linkingStatus = :linkingStatus ,"
            + " t.updatedUserId = :updatedUserId ,"
            + " t.updatedAt = now()"
            + " WHERE t.id = :fOrderId")
    int updateLinkingStatus(
            @Param("linkingStatus") FukukitaruMasterLinkingStatusType linkingStatus,
            @Param("fOrderId") BigInteger fOrderId,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 連携ステータスを更新する.
     * @param linkingStatus 連携ステータス
     * @param fOrderId フクキタル発注ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE TFOrderEntity t"
            + " SET linkingStatus = :linkingStatus ,"
            + " t.updatedUserId = :updatedUserId ,"
            + " t.updatedAt = now() ,"
            + " t.orderSendAt = now()"
            + " WHERE t.id = :fOrderId")
    int updateLinkingStatusOrderSendAt(
            @Param("linkingStatus") FukukitaruMasterLinkingStatusType linkingStatus,
            @Param("fOrderId") BigInteger fOrderId,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * フクキタル発注IDをキーにフクキタル発注情報テーブルから論理削除する.
     * @param fOrderId フクキタル発注ID
     * @param deletedAt 削除日
     * @return 更新件数
     */
    @Modifying
    @Query(value = " UPDATE TFOrderEntity t"
            + " SET t.deletedAt = :deletedAt"
            + " WHERE t.id = :fOrderId "
            + " AND t.deletedAt IS NULL")
    int updateDeleteAtByFOrderId(
            @Param("fOrderId") BigInteger fOrderId,
            @Param("deletedAt") Date deletedAt);

    /**
     * オーダー識別コードからフクキタル発注情報を取得.
     * @param orderCode オーダー識別コード
     * @return 資材発注情報
     */
    @Query("SELECT t FROM TFOrderEntity t"
            + " WHERE t.orderCode = :orderCode"
            + " AND t.deletedAt is null")
    Optional<TFOrderEntity> findByOrderCode(@Param("orderCode") String orderCode);

    /**
     * 確定ステータス、連携ステータスを更新する.
     * @param linkingStatus 連携ステータス
     * @param confirmStatus 確定ステータス
     * @param fOrderId フクキタル発注ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE TFOrderEntity t"
            + " SET linkingStatus = :linkingStatus ,"
            + " confirmStatus = :confirmStatus ,"
            + " t.updatedUserId = :updatedUserId ,"
            + " t.updatedAt = now() "
            + " WHERE t.id = :fOrderId")
    int updateLinkingStatusAndConfirmStatus(
            @Param("linkingStatus") FukukitaruMasterLinkingStatusType linkingStatus,
            @Param("confirmStatus") FukukitaruMasterConfirmStatusType confirmStatus,
            @Param("fOrderId") BigInteger fOrderId,
            @Param("updatedUserId") BigInteger updatedUserId);

}
