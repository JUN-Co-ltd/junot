package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.TReturnVoucherEntity;
import jp.co.jun.edi.type.SendMailStatusType;


/**
 * TReturnVoucherRepository.
 */
public interface TReturnVoucherRepository extends JpaRepository<TReturnVoucherEntity, BigInteger> {

    /**
     * 返品伝票管理から 引数で渡した状態の情報を取得する.
     * @param status 状態
     * @param pageable Pageable
     * @return メールテンプレートエンティティ
     */
    @Query("SELECT t FROM TReturnVoucherEntity t"
            + " WHERE t.status = :status"
            + "   AND t.deletedAt IS NULL")
    Page<TReturnVoucherEntity> findBySendStatus(
            @Param("status") SendMailStatusType status, Pageable pageable);

    /**
     * 返品伝票管理のステータスを更新する(複数用).
     * @param status ステータス
     * @param ids ID
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_returns_voucher t"
            + " SET t.status = :status, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.id IN (:ids)", nativeQuery = true)
    int updateStatusById(
            @Param("status") Integer status,
            @Param("ids") List<BigInteger> ids,
            @Param("userId") BigInteger userId);

    /**
     * 返品伝票管理のステータスを更新する.
     * @param status ステータス
     * @param id ID
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_returns_voucher t"
            + " SET t.status = :status, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.id = :id", nativeQuery = true)
    int updateStatusById(
            @Param("status") Integer status,
            @Param("id") BigInteger id,
            @Param("userId") BigInteger userId);
}
