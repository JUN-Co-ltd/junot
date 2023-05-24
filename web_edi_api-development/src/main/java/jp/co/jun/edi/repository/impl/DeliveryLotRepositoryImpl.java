package jp.co.jun.edi.repository.impl;

import java.math.BigInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import jp.co.jun.edi.repository.custom.DeliveryLotRepositoryCustom;

/**
 * DeliveryLotRepository実装クラス.
 */
public class DeliveryLotRepositoryImpl implements DeliveryLotRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public int sumDeliveryLotByOrderId(final BigInteger orderId, final BigInteger excludeDeliveryId) {
        final StringBuilder sql = new StringBuilder();

        sql.append("SELECT IFNULL(SUM(CASE WHEN dd.arrival_flg = '1' THEN ds.arrival_lot ELSE ds.delivery_lot END),0)");
        sql.append("  FROM t_delivery_sku ds");
        sql.append(" INNER JOIN t_delivery_detail dd");
        sql.append("       ON ds.delivery_detail_id = dd.id AND dd.deleted_at IS NULL");
        sql.append(" INNER JOIN t_delivery d");
        sql.append("        ON dd.delivery_id = d.id");
        if (excludeDeliveryId != null) {
            sql.append("        AND d.id <> :excludeDeliveryId");
        }
        sql.append("       AND d.deleted_at IS NULL");
        sql.append(" WHERE ds.deleted_at IS NULL");
        sql.append("   AND d.order_id = :orderId");

        final Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("orderId", orderId);
        if (excludeDeliveryId != null) {
            query.setParameter("excludeDeliveryId", excludeDeliveryId);
        }

        return ((Number) query.getSingleResult()).intValue();
    }
}
