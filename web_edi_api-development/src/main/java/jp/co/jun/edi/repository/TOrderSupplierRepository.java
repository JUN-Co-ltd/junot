package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TOrderSupplierEntity;
import jp.co.jun.edi.type.OrderCategoryType;

/**
 *
 * TOrderSupplierRepository.
 *
 */
@Repository
public interface TOrderSupplierRepository extends JpaRepository<TOrderSupplierEntity, BigInteger> {

    /**
     * 品番IDをキーに発注先メーカー情報を検索する.
     *
     * @param partNoId 品番ID
     * @param pageable pageable
     * @return 発注先メーカー情報を取得する
     */
    @Query("SELECT t FROM TOrderSupplierEntity t" + " WHERE t.partNoId = :partNoId AND t.deletedAt is null")
    Page<TOrderSupplierEntity> findByPartNoId(@Param("partNoId") BigInteger partNoId, Pageable pageable);

    /**
     * 現在の発注情報に紐づく発注先メーカー情報IDを取得する.
     * @param partNoId 品番ID
     * @param supplierCode メーカーコード
     * @param orderCategoryType 発注分類区分
     * @return 発注先メーカー情報ID
     */
    @Query("SELECT t.id "
            + "FROM TOrderSupplierEntity t "
            + "WHERE t.partNoId = :partNoId "
            + "AND t.supplierCode = :supplierCode "
            + "AND t.orderCategoryType = :orderCategoryType "
            + "AND t.deletedAt is null")
    BigInteger findIdByCurrentOrderInfo(@Param("partNoId") BigInteger partNoId,
            @Param("supplierCode") String supplierCode,
            @Param("orderCategoryType") OrderCategoryType orderCategoryType);

    /**
     * IDをキーに発注先メーカー情報を検索する.
     * @param id ID
     * @return 発注先メーカー情報
     */
    @Query("SELECT t FROM TOrderSupplierEntity t "
            + "WHERE t.id = :id "
            + "AND t.deletedAt IS NULL")
    Optional<TOrderSupplierEntity> findById(
            @Param("id") BigInteger id);
}
