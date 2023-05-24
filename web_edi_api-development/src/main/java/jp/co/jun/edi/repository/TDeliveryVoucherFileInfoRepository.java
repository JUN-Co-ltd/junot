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

import jp.co.jun.edi.entity.TDeliveryVoucherFileInfoEntity;
import jp.co.jun.edi.type.DeliveryVoucherCategoryType;
import jp.co.jun.edi.type.FileInfoStatusType;

/**
 *
 * TDeliveryVoucherFileInfoRepository.
 *
 */
@Repository
public interface TDeliveryVoucherFileInfoRepository
extends JpaRepository<TDeliveryVoucherFileInfoEntity, BigInteger> {
    /**
     * 指定した納品IDに紐づく納品伝票ファイル情報リストを取得する.
     * ただしfile_no_idがt_fileのidと紐付かないレコードは取得しない.
     *
     * @param deliveryId 納品ID
     * @return 納品伝票ファイル情報
     */
    @Query("SELECT dv FROM TDeliveryVoucherFileInfoEntity dv"
            + " WHERE dv.deliveryId = :deliveryId "
            + " AND dv.deletedAt IS NULL"
            + " AND EXISTS ("
            + "   SELECT f FROM TFileEntity f"
            + "   WHERE dv.fileNoId = f.id"
            + "   AND f.deletedAt IS NULL)")
    List<TDeliveryVoucherFileInfoEntity> findByDeliveryIdAndExistsTfile(
            @Param("deliveryId") BigInteger deliveryId);

    /**
     * 納品出荷伝票管理から 引数で渡した状態の情報を取得する.
     * @param status 状態
     * @param voucherCategory 伝票分類
     * @param pageable Pageable
     * @return メールテンプレートエンティティ
     */
    @Query("SELECT t FROM TDeliveryVoucherFileInfoEntity t"
            + " WHERE t.status = :status"
            + "   AND t.voucherCategory = :voucherCategory"
            + "   AND t.deletedAt IS NULL")
    Page<TDeliveryVoucherFileInfoEntity> findBySendStatus(
            @Param("status") FileInfoStatusType status,
            @Param("voucherCategory") DeliveryVoucherCategoryType voucherCategory,
            Pageable pageable);

    /**
     * 納品出荷伝票管理のステータスを更新する(複数用).
     * @param status ステータス
     * @param ids 納品出荷伝票IDの配列
     * @param userId ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delivery_voucher_file_info t"
            + " SET t.status = :status, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.id IN (:ids)", nativeQuery = true)
    int updateStatusByIds(
            @Param("status") Integer status,
            @Param("ids") List<BigInteger> ids,
            @Param("userId") BigInteger userId);

}
