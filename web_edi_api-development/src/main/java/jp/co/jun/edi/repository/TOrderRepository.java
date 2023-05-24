package jp.co.jun.edi.repository;

import java.math.BigDecimal;
import java.math.BigInteger;
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

import jp.co.jun.edi.entity.TOrderEntity;

/**
 * TOrderRepository.
 * orderNumberは受注確定されるまで、"000000"が設定されるため.
 * orderNumberを条件にしてSQLを実行すると想定外のレコードも.
 * 編集される可能性があるので、orderNumberは条件に極力使用しないこと.
 */
@Repository
public interface TOrderRepository extends JpaRepository<TOrderEntity, BigInteger>, JpaSpecificationExecutor<TOrderEntity> {

    /**
     * 発注Idから発注情報を取得.
     * @param orderId 発注Id
     * @return 発注情報
     */
    @Query("SELECT t FROM TOrderEntity t"
            + " WHERE t.id = :orderId"
            + " AND t.deletedAt is null")
    Optional<TOrderEntity> findByOrderId(
            @Param("orderId") BigInteger orderId);

    /**
     * 品番Idから発注情報リストを取得.
     * @param partNoId 品番Id
     * @return 発注情報リスト
     */
    @Query("SELECT t FROM TOrderEntity t"
            + " WHERE t.partNoId = :partNoId"
            + " AND t.deletedAt is null")
    List<TOrderEntity> findByPartNoId(
            @Param("partNoId") BigInteger partNoId);

    /**
     * 品番IDに紐づく、製品完納区分を条件にした発注情報リストを取得する.
     *
     * @param partNoId 品番ID
     * @param productCompleteOrder 完納区分
     * @return 発注情報リスト
     */
    @Query("SELECT t FROM TOrderEntity t"
            + " WHERE t.partNoId = :partNoId"
            + " AND t.productCompleteOrder = :productCompleteOrder"
            + " AND t.deletedAt is null")
    List<TOrderEntity> findByPartNoIdAndProductCompleteOrder(
            @Param("partNoId") BigInteger partNoId,
            @Param("productCompleteOrder") String productCompleteOrder);

    /**
     * 品番IDに紐づく、承認ステータスを条件にした発注情報リストを取得する.
     *
     * @param partNoId 品番ID
     * @param orderApproveStatusList 発注承認ステータス(複数)
     * @param pageable pageable
     * @return 発注情報リスト
     */
    @Query("SELECT t FROM TOrderEntity t"
            + " WHERE t.partNoId = :partNoId"
            + " AND t.orderApproveStatus in (:orderApproveStatusList)"
            + " AND t.deletedAt is null")
    Page<TOrderEntity> findByPartNoIdAndOrderApproveStatus(
            @Param("partNoId") BigInteger partNoId,
            @Param("orderApproveStatusList") String[] orderApproveStatusList,
            Pageable pageable
            );

    /**
     * 製品完納区分を更新する.
     * @param productCompleteOrder 製品完納区分
     * @param updatedUserId 更新ユーザID
     * @param id ID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_order t"
            + " SET product_complete_order = ?1,"
            + " updated_at = now(),"
            + " updated_user_id = ?2"
            + " WHERE t.id = ?3", nativeQuery = true)
    int updateProductCompleteOrder(
            @Param("productCompleteOrder") String productCompleteOrder,
            @Param("updatedUserId") BigInteger updatedUserId,
            @Param("id") BigInteger id
            );

    /**
     * 納品依頼回数を更新する.
     * @param deliveryCount 納品依頼回数
     * @param userId ユーザID
     * @param orderId 発注ID
     * @return 更新件数
     */
    @Modifying
    @Query("UPDATE TOrderEntity t"
            + " SET t.deliveryCount = :deliveryCount"
            + "   , t.updatedAt = now()"
            + "   , t.updatedUserId = :userId"
            + " WHERE t.id = :orderId"
            + "   AND t.deletedAt IS NULL")
    int updateDeliveryCount(
            @Param("deliveryCount") Integer deliveryCount,
            @Param("userId") BigInteger userId,
            @Param("orderId") BigInteger orderId);

    /**
     * @param orderId 発注ID
     * @param nonConformingProductUnitPrice B級品単価
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query("UPDATE TOrderEntity t"
            + " SET nonConformingProductUnitPrice = :nonConformingProductUnitPrice"
            + " , updatedUserId = :updatedUserId"
            + " WHERE t.id = :orderId")
    int updateNonConformingProductUnitPrice(
            @Param("orderId") BigInteger orderId,
            @Param("nonConformingProductUnitPrice") BigDecimal nonConformingProductUnitPrice,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 発注Idから単価情報を取得.
     * @param orderId 発注Id
     * @return 単価
     */
    @Query(value = " SELECT "
            + " CASE "
            + "   WHEN t.non_conforming_product_unit_price > 0 THEN t.non_conforming_product_unit_price "
            + "   WHEN t.non_conforming_product_unit_price IS NULL THEN t.unit_price "
            + "   WHEN t.non_conforming_product_unit_price =0 THEN t.unit_price "
            + "   ELSE 0 "
            + " END AS unit_price "
            + " FROM t_order t"
            + " WHERE t.id = :orderId"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    int findByOrderIdGetUnitPrice(
            @Param("orderId") BigInteger orderId);

    // PRD_0145 #10776 add JFE start
    /**
     * @param orderId 発注ID
     * @param newNecessaryLengthActual 実用尺
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query("UPDATE TOrderEntity t"
            + " SET necessaryLengthActual = :necessaryLengthActual"
            + " , updatedAt = now()"
            + " , updatedUserId = :updatedUserId"
            + " WHERE t.id = :orderId")
    int updateNecessaryLengthActual(
            @Param("orderId") BigInteger orderId,
            @Param("necessaryLengthActual") BigDecimal newNecessaryLengthActual,
            @Param("updatedUserId") BigInteger updatedUserId);
    // PRD_0145 #10776 add JFE end

    // PRD_0142 #10423 JFE add start
    /**
     * TAGDAT未作成の発注情報を取得する.
     * @return idリスト
     */
    @Query("SELECT t.id FROM TOrderEntity t"
            + " WHERE t.tagdatCreatedFlg = '0'"
            + " AND t.deletedAt is null")
    List<BigInteger> findOrder();

    /**
     * TAGDAT作成フラグとTAGDAT作成日を更新する.
     * @param id 発注ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query("UPDATE TOrderEntity t"
            + " SET t.tagdatCreatedFlg = '1'"
            + "   , t.tagdatCreatedAt = now()"
            + "   , t.updatedUserId = :updatedUserId"
            + "   , t.updatedAt = now()"
            + " WHERE t.id = :id ")
    int updateTagdatCreatedFlg(
            @Param("id") BigInteger id,
            @Param("updatedUserId") BigInteger updatedUserId);
    // PRD_0142 #10423 JFE add end
}
