package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.type.FileInfoStatusType;
/**
 *
 * TPurchaseRepository.
 *
 */
@Repository
public interface TPurchaseRepository extends JpaRepository<TPurchaseEntity, BigInteger> {

    /**
     * 納品IDをキーに仕入情報を検索する.
     * @param deliveryId 納品ID
     * @param pageable pageable
     * @return 仕入情報
     */
    @Query("SELECT t FROM TPurchaseEntity t"
         + " WHERE t.deliveryId = :deliveryId"
         + "   AND t.deletedAt is null")
    Page<TPurchaseEntity> findByDeliveryId(
            @Param("deliveryId") BigInteger deliveryId,
            Pageable pageable);

    /**
     * LG送信のキー(納品ID、課コード)リストで検索する.
     * @param deliveryIds 納品IDリスト
     * @param divisionCodes 課コードリスト
     * @return 仕入情報リスト
     */
    @Query("SELECT t FROM TPurchaseEntity t"
            + " WHERE t.deliveryId IN :deliveryIds"
            + "   AND t.divisionCode IN :divisionCodes"
            + "   AND t.deletedAt is null"
            + " ORDER BY t.purchaseCount"
            + "          ,t.divisionCode"
            + "          ,t.id")
    Optional<List<TPurchaseEntity>> findByLgKeyList(
            @Param("deliveryIds") List<BigInteger> deliveryIds,
            @Param("divisionCodes") List<String> divisionCodes);

    /**
     * 入荷日、仕入伝票No、仕入伝票行をキーに仕入情報を検索する.
     * @param arrivalAt 入荷日
     * @param purchaseVoucherNumber 仕入伝票No
     * @param purchaseVoucherLine 仕入伝票行
     * @return 仕入情報
     */
    @Query("SELECT t FROM TPurchaseEntity t"
            + " WHERE t.arrivalAt = :arrivalAt "
            + "    AND t.purchaseVoucherNumber  = :purchaseVoucherNumber"
            + "    AND t.purchaseVoucherLine  = :purchaseVoucherLine"
            + "    AND t.deletedAt IS NULL")
    Optional<TPurchaseEntity> findByManageNumAndVoucherNumAndVoucherLine(
            @Param("arrivalAt") Date arrivalAt,
            @Param("purchaseVoucherNumber") String purchaseVoucherNumber,
            @Param("purchaseVoucherLine") Integer purchaseVoucherLine);

    // PRD_0017 add SIT start
    /**
     * 仕入伝票No、仕入伝票行をキーに仕入情報を検索する.
     * @param purchaseVoucherNumber 仕入伝票No
     * @param purchaseVoucherLine 仕入伝票行
     * @return 仕入情報
     */
    @Query("SELECT t FROM TPurchaseEntity t"
            + " WHERE t.purchaseVoucherNumber  = :purchaseVoucherNumber"
            + "    AND t.purchaseVoucherLine  = :purchaseVoucherLine"
            + "    AND t.deletedAt IS NULL")
    Optional<TPurchaseEntity> findByVoucherNumAndVoucherLine(
            @Param("purchaseVoucherNumber") String purchaseVoucherNumber,
            @Param("purchaseVoucherLine") Integer purchaseVoucherLine);
    // PRD_0017 add SIT end

    // PRD_0201 add JFE start
    /**
     * 仕入伝票No、仕入伝票行、仕入先、計上日をキーに仕入情報を検索する.
     * @param purchaseVoucherNumber 仕入伝票No
     * @param purchaseVoucherLine 仕入伝票行
     * @param supplierCode 仕入先
     * @param recordAt 計上日
     * @return 仕入情報
     */
    @Query("SELECT t FROM TPurchaseEntity t"
            + " WHERE t.purchaseVoucherNumber  = :purchaseVoucherNumber"
            + "    AND t.purchaseVoucherLine  = :purchaseVoucherLine"
            + "    AND t.supplierCode  = :supplierCode"
            + "    AND t.recordAt  = :recordAt"
            + "    AND t.deletedAt IS NULL")
    Optional<TPurchaseEntity> findByVoucherNumAndVoucherLineAndSupplierCodeAndRecordAt(
            @Param("purchaseVoucherNumber") String purchaseVoucherNumber,
            @Param("purchaseVoucherLine") Integer purchaseVoucherLine,
            @Param("supplierCode") String supplierCode,
            @Param("recordAt") Date recordAt);
    // PRD_0201 add JFE end

    //PRD_0202 add JFE start
    /**
     * 仕入伝票No、仕入伝票行、発注IDをキーに仕入情報を検索する.
     * @param purchaseVoucherNumber 仕入伝票No
     * @param purchaseVoucherLine 仕入伝票行
     * @param orderId 発注ID
     * @return 仕入情報
     */
    @Query("SELECT t FROM TPurchaseEntity t"
            + " WHERE t.purchaseVoucherNumber  = :purchaseVoucherNumber"
            + "    AND t.purchaseVoucherLine  = :purchaseVoucherLine"
            + "    AND t.orderId  = :orderId"
            + "    AND t.deletedAt IS NULL")
    Optional<TPurchaseEntity> findByVoucherNumAndVoucherLineAndOrderId(
            @Param("purchaseVoucherNumber") String purchaseVoucherNumber,
            @Param("purchaseVoucherLine") Integer purchaseVoucherLine,
            @Param("orderId") BigInteger orderId);
    //PRD_0202 add JFE end

    /**
     * 会計連携ステータスと日計日をキーに計上日が指定範囲内の仕入情報を検索する.
     * ・計上日あり
     * ・入荷確定数あり
     * ・計上日が指定範囲内(日計日～過去指定日数分)
     * ※ネイティブクエリだとFileInfoStatusTypeが変換できないため、accountLinkingStatusはintで受け取る
     *
     * @param accountLinkingStatus 会計連携ステータス
     * @param nitymd 日計日
     * @param recordAtRange 計上日指定範囲
     * @param pageable pageable
     * @return 仕入情報
     */
    @Query(value = "SELECT t.* FROM t_purchase t"
         + " WHERE t.account_linking_status = :accountLinkingStatus"
         + "   AND t.record_at IS NOT NULL"
         + "   AND t.fix_arrival_count IS NOT NULL"
         + "   AND t.record_at BETWEEN DATE_SUB(:nitymd,INTERVAL :recordAtRange DAY) AND :nitymd"
         + "   AND t.deleted_at IS NULL", nativeQuery = true)
    Page<TPurchaseEntity> findByAccountLinkingStatusAndNitymd(
            @Param("accountLinkingStatus") int accountLinkingStatus,
            @Param("nitymd") Date nitymd,
            @Param("recordAtRange") int recordAtRange,
            Pageable pageable);

    /**
     * IDリストを基に会計連携ステータスを更新する.
     *
     * @param accountLinkingStatus 会計連携ステータス
     * @param ids 仕入情報IDリスト
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE TPurchaseEntity t"
            + " SET t.accountLinkingStatus = :accountLinkingStatus ,"
            + " t.updatedUserId = :updatedUserId ,"
            + " t.updatedAt = now()"
            + " WHERE t.id IN (:ids)"
            + " AND t.deletedAt is null")
    int updateAccountLinkingStatusByIds(
            @Param("accountLinkingStatus") FileInfoStatusType accountLinkingStatus,
            @Param("ids") List<BigInteger> ids,
            @Param("updatedUserId") BigInteger updatedUserId);

}
