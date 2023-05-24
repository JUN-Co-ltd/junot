package jp.co.jun.edi.repository;

import java.math.BigInteger;
//import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TMakerReturnEntity;

/**
 * TMakerReturnRepository.
 */
@Repository
public interface TMakerReturnRepository extends JpaRepository<TMakerReturnEntity, BigInteger> {
    /**
     * 伝票番号と発注IDに紐づくメーカー返品情報の1件目を取得.
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @return メーカー返品情報
     */
    @Query("SELECT t FROM TMakerReturnEntity t"
            + " WHERE t.voucherNumber = :voucher_number"
            + " AND t.orderId = :order_id"
            + " AND t.voucherLine = 1"
            + " AND t.deletedAt is null")
    Optional<TMakerReturnEntity> findByVoucherNumberAndOrderIdAndVoucherLine1(
            @Param("voucher_number") String voucherNumber,
            @Param("order_id") BigInteger orderId);

    /**
     * 伝票番号と発注IDをキーにメーカー返品情報リストを検索する.
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @return メーカー返品情報
     */
    @Query("SELECT t FROM TMakerReturnEntity t"
            + " WHERE t.voucherNumber = :voucherNumber"
            + "   AND t.orderId= :orderId"
            + "   AND t.deletedAt IS NULL")
    Optional<List<TMakerReturnEntity>> findByVoucherNumberAndOrderId(
            @Param("voucherNumber") String voucherNumber,
            @Param("orderId") BigInteger orderId);

    /**
     * IDをキーにメーカー返品情報リストの削除日を現在日時で更新する.
     * @param ids IDリスト
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_maker_return t"
            + " SET t.deleted_at = NOW()"
            + " ,t.updated_at = NOW()"
            + " ,t.updated_user_id = :updatedUserId"
            + " WHERE t.id IN :ids"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    int updateDeletedAtByIds(
            @Param("ids") List<BigInteger> ids,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * LG送信のキー(発注ID、管理番号)リストで検索する.
     * @param orderId 発注IDリスト
     * @param voucherNumber 管理番号リスト
     * @return メーカー返品指示リスト
     */
    @Query("SELECT t FROM TMakerReturnEntity t"
            + " WHERE t.orderId IN :orderId"
            + "   AND t.voucherNumber IN :voucherNumber"
            + "   AND t.deletedAt is null"
            + " ORDER BY t.voucherNumber"
            + "          ,t.id")
    Optional<List<TMakerReturnEntity>> findByLgKeyList(
            @Param("orderId") List<BigInteger> orderId,
            @Param("voucherNumber") List<String> voucherNumber);

    /**
     * @param wmsLinkingFileId 倉庫連携ファイルID
     * @return 件数
     */
    @Query("SELECT COUNT(t.id) FROM TMakerReturnEntity t"
            + " WHERE t.wmsLinkingFileId = :wmsLinkingFileId"
            + "   AND t.deletedAt IS NULL")
    int countByWmsLinkingFileId(@Param("wmsLinkingFileId") BigInteger wmsLinkingFileId);

//    #6783 mod JFE start
    /**
     * メーカー返品指示確定ファイルから更新対象のt_maker_return情報を検索.
     *
     * @param returnAt 返品日
     * @param voucherNumber 伝票番号
     * @param voucherLine 伝票行
     * @return メーカー返品情報
     */
    @Query(value = "SELECT t.* FROM t_maker_return t"
            + " WHERE t.voucher_number = :voucherNumber"
            + "   AND t.voucher_line = :voucherLine"
            + "   AND t.deleted_at IS NULL", nativeQuery = true)
    Optional<TMakerReturnEntity> findByManageColumnAndSequence(
            @Param("voucherNumber") String voucherNumber,
            @Param("voucherLine") Integer voucherLine
            );
//    #6783 mod JFE end
}
