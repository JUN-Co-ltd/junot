package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.DeliveryCompositeEntity;

/**
 * 納品情報Repository.
 */
@Repository
public interface DeliveryCompositeRepository extends JpaRepository<DeliveryCompositeEntity, BigInteger> {

    /**
     * 発注ID配列から 納品情報を検索する.
     * @param orderIds 発注ID配列
     * @return 納品情報リスト
     */
    @Query(value = "SELECT"
            + " d.id AS id"
            + ",d.order_id AS order_id"
            + ",d.order_number AS order_number"
            + ",d.part_no_id AS part_no_id"
            + ",d.part_no AS part_no"
            + ",d.delivery_count AS delivery_count"
            + ",d.last_delivery_status AS last_delivery_status"
            + ",d.delivery_approve_status AS delivery_approve_status"
            + ",d.delivery_approve_at AS delivery_approve_at"
            + ",dd.delivery_request_at AS delivery_request_at"
            + ",dd.correction_at AS correction_at"
            + ",IFNULL(SUM(ds.delivery_lot), 0) AS sum_delivery_lot"
            + " FROM"
            + " t_delivery d"
            + " INNER JOIN t_delivery_detail dd"
            + " ON dd.delivery_id = d.id"
            + " AND dd.deleted_at IS NULL"
            + " INNER JOIN t_delivery_sku ds"
            + " ON ds.delivery_detail_id = dd.id"
            + " AND ds.deleted_at IS NULL"
            + " WHERE"
            + " d.order_id IN (:orderIds)"
            + " AND d.deleted_at IS NULL"
            + " GROUP BY"
            + " d.id", nativeQuery = true)
    List<DeliveryCompositeEntity> findByOrderIds(
            @Param("orderIds") List<BigInteger> orderIds);
}
