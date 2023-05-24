package jp.co.jun.edi.repository;

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

import jp.co.jun.edi.entity.TDeliveryStoreEntity;

/**
 * TDeliveryStoreRequestRepository.
 */
@Repository
public interface TDeliveryStoreRepository extends JpaRepository<TDeliveryStoreEntity, BigInteger>, JpaSpecificationExecutor<TDeliveryStoreEntity> {
   /**
    * 納品明細IDをキーに納品得意先情報を検索する.
    *
    * @param deliveryDetailId 納品明細ID
    * @param pageable pageable
    * @return 納品得意先情報
    */
   @Query("SELECT t FROM TDeliveryStoreEntity t"
           + " WHERE t.deliveryDetailId = :deliveryDetailId "
           + " AND t.deletedAt is null")
   Page<TDeliveryStoreEntity> findByDeliveryDetailId(
           @Param("deliveryDetailId") BigInteger deliveryDetailId,
           Pageable pageable);

   /**
    * 納品IDをキーに納品得意先情報を検索し、一意の店舗コードリストを取得する.
    *
    * @param deliveryId 納品ID
    * @return 納品得意先情報
    */
   @Query("SELECT DISTINCT(dst.storeCode) FROM TDeliveryStoreEntity dst"
           + " WHERE dst.deliveryDetailId IN"
           + " ( "
           + "   SELECT dd.id FROM TDeliveryDetailEntity dd"
           + "   WHERE dd.deliveryId = :deliveryId "
           + "   AND dd.deletedAt is null"
           + " )"
           + " AND dst.deletedAt is null")
   List<String> findDistinctStoreCodeByDeliveryId(
           @Param("deliveryId") BigInteger deliveryId);

   /**
    * 納品明細IDリストをキーに納品得意先情報を論理削除する.
    *
    * @param ids 納品明細IDリスト
    * @param updatedUserId 更新ユーザID
    * @return 更新件数
    */
   @Modifying
   @Query(value = "UPDATE t_delivery_store t"
           + " SET t.deleted_at = now()"
           + "     ,t.updated_user_id = :updatedUserId"
           + "     ,t.updated_at = now()"
           + " WHERE t.delivery_detail_id IN (:ids)"
           + " AND t.deleted_at IS NULL", nativeQuery = true)
   int updateDeletedAtByDeliveryDetailIds(
           @Param("ids") List<BigInteger> ids,
           @Param("updatedUserId") BigInteger updatedUserId);

   /**
    * 納品明細ID,除外納品得意先ID配列をキーに納品得意先情報を論理削除する.
    *
    * @param deliveryDetailId 納品明細ID
    * @param excludeDeliveryStoreIds 除外納品得意先ID配列
    * @param updatedUserId 更新ユーザID
    * @return 更新件数
    */
   @Modifying
   @Query(value = "UPDATE t_delivery_store t"
           + " SET t.deleted_at = now()"
           + "     ,t.updated_user_id = :updatedUserId"
           + "     ,t.updated_at = now()"
           + " WHERE t.delivery_detail_id = :deliveryDetailId"
           + "   AND t.id NOT IN (:excludeDeliveryStoreIds)"
           + "   AND t.deleted_at IS NULL", nativeQuery = true)
   int updateDeletedAtByDeliveryDetailIdAndExclusionIds(
           @Param("deliveryDetailId") BigInteger deliveryDetailId,
           @Param("excludeDeliveryStoreIds") List<BigInteger> excludeDeliveryStoreIds,
           @Param("updatedUserId") BigInteger updatedUserId);

   /**
    * 納品IDをキーに納品得意先情報を論理削除する.
    *
    * @param deliveryId 納品ID
    * @param updatedUserId 更新ユーザID
    * @return 更新件数
    */
   @Modifying
   @Query(value = "UPDATE t_delivery_store dst"
           + "   LEFT JOIN t_delivery_detail dd "
           + "          ON dd.id = dst.delivery_detail_id"
           + " SET dst.deleted_at = now()"
           + "     ,dst.updated_user_id = :updatedUserId"
           + "     ,dst.updated_at = now()"
           + " WHERE dd.delivery_id = :deliveryId"
           + "   AND dst.deleted_at IS NULL", nativeQuery = true)
   int updateDeletedAtByDeliveryId(
           @Param("deliveryId") BigInteger deliveryId,
           @Param("updatedUserId") BigInteger updatedUserId);

   /**
    * idで論理削除されていない納品得意先情報を取得する.
    * @param id id
    * @return 納品得意先情報
    */
   Optional<TDeliveryStoreEntity> findByIdAndDeletedAtIsNull(@Param("id") BigInteger id);

}
