package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.TOrderSendMailEntity;

/**
 * TOrderSendMailRepository.
 */
public interface TOrderSendMailRepository extends JpaRepository<TOrderSendMailEntity, BigInteger> {

    /**
     * 受注確定情報メール送信管理から 送信状態が「未送信」の情報を取得する.
     * @param pageable Pageable
     * @return メールテンプレートエンティティ
     */
    @Query("SELECT t FROM TOrderSendMailEntity t"
            + " WHERE t.status = 0"
            + " AND t.deletedAt IS NULL")
    Page<TOrderSendMailEntity> findBySendStatus(Pageable pageable);

    /**
     * 受注確定情報メール送信管理のステータスを更新する.
     * @param status ステータス
     * @param ids 受注確定情報メール送信管理IDリスト
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_send_mail t"
            + " SET t.status = :status, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.id IN (:ids)", nativeQuery = true)
    int updateStatusById(
            @Param("status") Integer status,
            @Param("ids") List<BigInteger> ids,
            @Param("userId") BigInteger userId);

    /**
     * 受注確定情報メール送信管理のステータスを更新する.
     * @param status ステータス
     * @param id 受注確定情報メール送信管理ID
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order_send_mail t"
            + " SET t.status = :status, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.id = :id", nativeQuery = true)
    int updateStatusById(
            @Param("status") Integer status,
            @Param("id") BigInteger id,
            @Param("userId") BigInteger userId);
}
