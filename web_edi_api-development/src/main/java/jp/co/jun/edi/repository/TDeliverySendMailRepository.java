package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TDeliverySendMailEntity;

/**
 *
 * TDeliverySendMailRepository.
 *
 */
@Repository
public interface TDeliverySendMailRepository extends JpaRepository<TDeliverySendMailEntity, BigInteger> {

    /**
     * 納品IDから、納品依頼メール送信情報を取得する.
     * @param deliveryId 納品ID
     * @param pageable {@link Pageable} instance
     * @return 納品依頼メール送信情報
     */
    @Query("SELECT t FROM TDeliverySendMailEntity t"
            + " WHERE t.deletedAt is null"
            + " AND t.deliveryId = :deliveryId")
    Page<TDeliverySendMailEntity> findByDeliveryId(
           @Param("deliveryId") BigInteger deliveryId, Pageable pageable);

    /**
     * 納品IDから送信状態を更新する.
     *
     * @param status 送信状態
     * @param updatedUserId 更新ユーザID
     * @param sendMailId メール送信ID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_send_mail t"
            + " SET t.status = :status,"
            + " t.updated_user_id = :updatedUserId ,"
            + " t.updated_at = now()"
            + " WHERE t.id = :sendMailId", nativeQuery = true)
    int updateStatus(
            @Param("status") int status,
            @Param("updatedUserId") BigInteger updatedUserId,
            @Param("sendMailId") BigInteger sendMailId);



    /**
     * 納品依頼メール送信管理のステータスを更新する.
     * @param status ステータス
     * @param ids 納品依頼メール送信管理IDリスト
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_send_mail t"
            + " SET t.status = :status, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.id IN (:ids)", nativeQuery = true)
    int updateStatusById(
            @Param("status") Integer status,
            @Param("ids") List<BigInteger> ids,
            @Param("userId") BigInteger userId);

    /**
     * 納品承認情報メール送信管理から 送信状態が「未送信」の情報を取得する.
     * @param pageable Pageable
     * @return メールテンプレートエンティティ
     */
    @Query("SELECT t FROM TDeliverySendMailEntity t"
            + " WHERE t.status = 0"
            + " AND t.deletedAt IS NULL")
    Page<TDeliverySendMailEntity> findBySendStatus(Pageable pageable);
}
