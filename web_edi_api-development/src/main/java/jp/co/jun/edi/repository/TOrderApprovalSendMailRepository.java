package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.TOrderApprovalSendMailEntity;

/**
 * TOrderApprovalSendMailRepository.
 */
public interface TOrderApprovalSendMailRepository extends JpaRepository<TOrderApprovalSendMailEntity, BigInteger> {

    /**
     * 発注承認情報メール送信管理から 送信状態が「未送信」の情報を取得する.
     * @param pageable Pageable
     * @return メールテンプレートエンティティ
     */
    @Query("SELECT t FROM TOrderApprovalSendMailEntity t"
            + " WHERE t.status = 0"
            + " AND t.deletedAt IS NULL")
    Page<TOrderApprovalSendMailEntity> findBySendStatus(Pageable pageable);

    /**
     * 発注承認情報メール送信管理のステータスを更新する.
     * @param status ステータス
     * @param ids 発注承認情報メール送信管理IDリスト
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_approval_send_mail t"
            + " SET t.status = :status, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.id IN (:ids)", nativeQuery = true)
    int updateStatusById(
            @Param("status") Integer status,
            @Param("ids") List<BigInteger> ids,
            @Param("userId") BigInteger userId);

    /**
     * 発注承認情報メール送信管理のステータスを更新する.
     * @param status ステータス
     * @param id 発注承認情報メール送信管理ID
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_approval_send_mail t"
            + " SET t.status = :status, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.id = :id", nativeQuery = true)
    int updateStatusById(
            @Param("status") Integer status,
            @Param("id") BigInteger id,
            @Param("userId") BigInteger userId);
}
