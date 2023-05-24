package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.TOrderApprovalOfficialSendMailEntity;
import jp.co.jun.edi.type.SendMailStatusType;

/**
 * TOrderApprovalOfficialSendMailRepository.
 */
public interface TOrderApprovalOfficialSendMailRepository extends JpaRepository<TOrderApprovalOfficialSendMailEntity, BigInteger> {

    /**
     * 発注承認正式情報メール送信管理から 送信状態が「未送信」の情報を取得する.
     * @param pageable Pageable
     * @param status ステータス
     * @return メールテンプレートエンティティ
     */
    @Query("SELECT t FROM TOrderApprovalOfficialSendMailEntity t"
            + " WHERE t.status = :status"
            + " AND t.deletedAt IS NULL")
    Page<TOrderApprovalOfficialSendMailEntity> findBySendStatus(@Param("status") SendMailStatusType status, Pageable pageable);

    /**
     * 発注IDから発注承認正式情報メール送信管理情報を取得する.
     * @param orderId 発注ID
     * @return メールテンプレートエンティティ
     */
    @Query("SELECT t FROM TOrderApprovalOfficialSendMailEntity t"
            + " WHERE t.deletedAt IS NULL"
            + " AND t.status = 0"
            + " AND t.orderId = :orderId")
    Optional<TOrderApprovalOfficialSendMailEntity> findByOrderId(@Param("orderId") BigInteger orderId);

    /**
     * 発注承認正式情報メール送信管理のステータスを更新する.
     * @param status ステータス
     * @param ids 発注承認正式情報メール送信管理IDリスト
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_approval_official_send_mail t"
            + " SET t.status = :status, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.id IN (:ids)", nativeQuery = true)
    int updateStatusByIds(
            @Param("status") Integer status,
            @Param("ids") List<BigInteger> ids,
            @Param("userId") BigInteger userId);

    /**
     * 発注承認正式情報メール送信管理のステータスを更新する.
     * ステータスが1かつ、指定のIDのものを対象とする.
     * @param status ステータス
     * @param ids 発注承認正式情報メール送信管理IDリスト
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_approval_official_send_mail t"
            + " SET t.status = :status, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.id IN (:ids)"
            + " AND t.status = 1", nativeQuery = true)
    int updateStatusByIdsStatusOne(
            @Param("status") Integer status,
            @Param("ids") List<BigInteger> ids,
            @Param("userId") BigInteger userId);

    /**
     * 発注承認正式情報メール送信管理のステータスを更新する.
     * @param status ステータス
     * @param id 発注承認正式情報メール送信管理ID
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_approval_official_send_mail t"
            + " SET t.status = :status, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.id = :id", nativeQuery = true)
    int updateStatusById(
            @Param("status") Integer status,
            @Param("id") BigInteger id,
            @Param("userId") BigInteger userId);

}
