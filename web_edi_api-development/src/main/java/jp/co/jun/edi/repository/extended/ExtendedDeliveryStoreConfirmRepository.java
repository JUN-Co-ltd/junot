package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TDeliveryStoreSkuEntity;

/**
 * DeliveryStoreConfirmRepository.
 */
@Repository
public interface ExtendedDeliveryStoreConfirmRepository
extends JpaRepository<TDeliveryStoreSkuEntity, BigInteger> {
    /**
     * 送信対象レコード取得.
     * @param strIds 納品明細ID一覧
     * @return 得意先SKU一覧
     */
    @Query(value = " SELECT "
            + " dss.*"
            + " FROM t_delivery_store_sku dss "
            + " INNER JOIN t_delivery_store ds "
            + " ON dss.delivery_store_id = ds.id"
            + " WHERE 1=1 "
            + " AND ds.delivery_detail_id in (:strIds)"
            + " AND ds.deleted_at is NULL"
            + " AND dss.deleted_at is NULL"
            + " ORDER BY "
            + " ds.delivery_detail_id ASC"
            + " , ds.id ASC"
            + " , dss.id ASC", nativeQuery = true)
    List<TDeliveryStoreSkuEntity> findByDeliveryDetailIds(
            @Param("strIds") String strIds);
}
