package jp.co.jun.edi.repository.impl;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import jp.co.jun.edi.entity.VDelischeDeliveryRequestEntity;
import jp.co.jun.edi.repository.constants.DelischeConstants;
import jp.co.jun.edi.repository.custom.VDelischeDeliveryRequestRepositoryCustom;
import jp.co.jun.edi.util.DateUtils;

/**
 * デリスケ納品依頼Repository実装クラス.
 */
public class VDelischeDeliveryRequestRepositoryImpl implements VDelischeDeliveryRequestRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * デリスケ納品依頼情報を取得する.
     *
     * <pre>
     * {@code
     * SELECT o.id as order_id
     *      , d.id as delivery_id
     *      , dd.delivery_count as delivery_count
     *      , dd.correction_at as delivery_at
     *      , CASE WHEN DATE_FORMAT(dd.correction_at, '%e') >= 21
     *             THEN CASE DATE_FORMAT(dd.correction_at, '%c')
     *                  WHEN 12 THEN 1
     *                  ELSE DATE_FORMAT(dd.correction_at, '%c') + 1 END
     *             ELSE DATE_FORMAT(dd.correction_at, '%c')
     *        END as delivery_at_monthly
     *      , i.brand_code as brand_code
     *      , i.item_code as item_code
     *      , i.part_no as part_no
     *      , i.product_name as product_name
     *      , CASE i.sub_season_code
     *             WHEN 1 THEN 'A1'
     *             WHEN 2 THEN 'A2'
     *             WHEN 5 THEN 'B1'
     *             WHEN 6 THEN 'B2'
     *             WHEN 9 THEN 'C'
     *             ELSE NULL
     *         END as season
     *      , o.mdf_maker_code as mdf_maker_code
     *      , sm.name as mdf_maker_name
     *      , o.product_order_at as product_order_at
     *      , o.product_delivery_at as product_delivery_at
     *      , CASE WHEN o.product_delivery_at < dd.correction_at
     *              AND (
     *                   d.delivery_date_change_reason_id IN ('2', '9')
     *                   OR d.delivery_date_change_reason_id IS NULL
     *                  )
     *             THEN '1'
     *             ELSE '0' END as late_delivery_at_flg
     *      , (
     *         SELECT
     *           SUM(pol_os.product_order_lot)
     *         FROM
     *           t_order_sku pol_os
     *         WHERE
     *           pol_os.order_id = d.order_id
     *           AND pol_os.deleted_at IS NULL
     *           AND EXISTS (
     *             SELECT
     *               pol_ds.id
     *             FROM
     *               t_delivery_sku pol_ds
     *               INNER JOIN t_delivery_detail pol_dd
     *                 ON pol_ds.delivery_detail_id = pol_dd.id
     *                 AND pol_dd.deleted_at IS NULL
     *             WHERE
     *               pol_dd.delivery_id = d.id
     *               AND pol_ds.color_code = pol_os.color_code
     *               AND pol_ds.size = pol_os.size
     *           )
     *        ) as product_order_lot_sum
     *      , (
     *         SELECT
     *           SUM(dl_ds.delivery_lot)
     *         FROM
     *           t_delivery_sku dl_ds
     *           INNER JOIN t_delivery_detail dl_dd
     *             ON dl_ds.delivery_detail_id = dl_dd.id
     *             AND dl_dd.deleted_at IS NULL
     *         WHERE
     *           dl_dd.delivery_id = d.id
     *           AND dl_ds.deleted_at IS NULL
     *        ) as delivery_lot_sum
     *      , (
     *         SELECT
     *           SUM(al_ds.arrival_lot)
     *         FROM
     *           t_delivery_sku al_ds
     *           INNER JOIN t_delivery_detail al_dd
     *             ON al_ds.delivery_detail_id = al_dd.id
     *             AND al_dd.deleted_at IS NULL
     *         WHERE
     *           al_dd.delivery_id = d.id
     *           AND al_ds.deleted_at IS NULL
     *        ) as arrival_lot_sum
     *      , o.retail_price
     *      , o.product_cost
     *      , o.order_approve_status as order_approve_status
     *      , i.quality_composition_status as quality_composition_status
     *      , i.quality_coo_status as quality_coo_status
     *      , i.quality_harmful_status as quality_harmful_status
     * FROM t_order o
     *   INNER JOIN t_delivery d
     *           ON d.order_id = o.id
     *          AND d.deleted_at IS NULL
     *   INNER JOIN t_delivery_detail dd
     *           ON d.id = dd.delivery_id
     *          AND dd.deleted_at IS NULL
     *          AND :deliveryAtFrom <= dd.correction_at
     *          AND :deliveryAtDateFrom <= dd.correction_at
     *          AND dd.correction_at <= :deliveryAtTo
     *          AND dd.correction_at <= :deliveryAtDateTo
     *          AND o.product_delivery_at < dd.correction_at
     *          AND (
     *               d.delivery_date_change_reason_id IN ('2', '9')
     *               OR d.delivery_date_change_reason_id IS NULL
     *              )
     *   INNER JOIN t_item i
     *           ON o.part_no_id = i.id
     *          AND i.deleted_at IS NULL
     *   LEFT OUTER JOIN m_sirmst sm
     *           ON o.mdf_maker_code = sm.sire
     *          AND sm.deleted_at IS NULL
     *          AND sm.mntflg IN ('1', '2', '')
     *   LEFT OUTER JOIN m_codmst c
     *           ON c.code1 = o.mdf_staff_code
     *          AND c.tblid = '22'
     *          AND c.item5 = '1'
     *          AND c.deleted_at IS NULL
     *          AND c.mntflg IN ('1', '2', '')
     * WHERE o.deleted_at IS NULL
     *   AND o.id = :orderId
     * GROUP BY o.id
     *        , d.id
     * ORDER BY delivery_at DESC
     *        , delivery_id DESC
     *        , delivery_count ASC
     * }
     * </pre>
     */
    @Override
    public List<VDelischeDeliveryRequestEntity> findBySpec(final BigInteger orderId, final Date deliveryAtFrom,
            final Date deliveryAtTo, final Date deliveryAtDateFrom, final Date deliveryAtDateTo, final boolean deliveryAtLateType) {

        final StringBuilder sql = new StringBuilder();
        generateSelectPhrase(sql);
        generateFromPhrase(deliveryAtFrom, deliveryAtTo, deliveryAtDateFrom, deliveryAtDateTo, deliveryAtLateType, sql);

        final Query createNativeQuery = entityManager.createNativeQuery(sql.toString(), VDelischeDeliveryRequestEntity.class);

        createNativeQuery.setParameter("orderId", orderId);
        if (!Objects.isNull(deliveryAtFrom)) {
            createNativeQuery.setParameter("deliveryAtFrom", DateUtils.truncateDate(deliveryAtFrom));
        }
        if (!Objects.isNull(deliveryAtDateFrom)) {
            createNativeQuery.setParameter("deliveryAtDateFrom", DateUtils.truncateDate(deliveryAtDateFrom));
        }
        if (!Objects.isNull(deliveryAtTo)) {
            createNativeQuery.setParameter("deliveryAtTo", DateUtils.truncateDate(deliveryAtTo));
        }
        if (!Objects.isNull(deliveryAtDateTo)) {
            createNativeQuery.setParameter("deliveryAtDateTo", DateUtils.truncateDate(deliveryAtDateTo));
        }

        @SuppressWarnings("unchecked")
        final List<VDelischeDeliveryRequestEntity> deliveryReqEntity = createNativeQuery.getResultList();

        return deliveryReqEntity;
    }

    /**
     * SELECT句を作成する.
     * @param sql sql
     */
    private void generateSelectPhrase(final StringBuilder sql) {
        sql.append("SELECT o.id as order_id");
        sql.append("     , d.id as delivery_id");
        sql.append("     , dd.delivery_count as delivery_count");
        sql.append("     , dd.correction_at as delivery_at");
        sql.append("     , ");
        sql.append(DelischeConstants.DELIVERY_AT_MONTHLY_SQL);
        sql.append("       as delivery_at_monthly");
        sql.append("     , i.brand_code as brand_code");
        sql.append("     , i.item_code as item_code");
        sql.append("     , i.part_no as part_no");
        sql.append("     , i.product_name as product_name");
        sql.append("     , ");
        sql.append(DelischeConstants.SEASON_SQL);
        sql.append("       as season");
        sql.append("     , o.mdf_maker_code as mdf_maker_code");
        sql.append("     , sm.name as mdf_maker_name");
        sql.append("     , o.product_order_at as product_order_at");
        sql.append("     , o.product_delivery_at as product_delivery_at");
        sql.append("     , ");
        sql.append(DelischeConstants.LATE_DELIVERY_AT_FLG_SQL);
        sql.append("       as late_delivery_at_flg");
        sql.append("     , ");
        sql.append(DelischeConstants.PRODUCT_ORDER_LOT_SUM_SUB_QUERY);
        sql.append("       as product_order_lot_sum");
        sql.append("     , ");
        sql.append(DelischeConstants.DELIVERY_LOT_SUM_SUB_QUERY);
        sql.append("       as delivery_lot_sum");
        sql.append("     , ");
        sql.append(DelischeConstants.ARRIVAL_LOT_SUM_SUB_QUERY);
        sql.append("       as arrival_lot_sum");
        sql.append("     , o.retail_price");
        sql.append("     , o.product_cost");
        sql.append("     , o.order_approve_status as order_approve_status");
        sql.append("     , i.quality_composition_status as quality_composition_status");
        sql.append("     , i.quality_coo_status as quality_coo_status");
        sql.append("     , i.quality_harmful_status as quality_harmful_status");
    }

    /**
     * FROM句を作成する.
     * @param deliveryAtFrom 納期from
     * @param deliveryAtTo 納期to
     * @param deliveryAtDateFrom 納期from(年度・納品週から作成)
     * @param deliveryAtDateTo 納期to(年度・納品週から作成)
     * @param isDeliveryAtLate 納期遅延区分
     * @param sql sql
     */
    private void generateFromPhrase(final Date deliveryAtFrom, final Date deliveryAtTo, final Date deliveryAtDateFrom, final Date deliveryAtDateTo,
            final boolean isDeliveryAtLate, final StringBuilder sql) {
        sql.append(" FROM t_order o");
        sql.append("   INNER JOIN t_delivery d");
        sql.append("           ON d.order_id = o.id");
        sql.append("          AND d.deleted_at IS NULL");
        sql.append("   INNER JOIN t_delivery_detail dd");
        sql.append("           ON d.id = dd.delivery_id");
        sql.append("          AND dd.deleted_at IS NULL");

        if (!Objects.isNull(deliveryAtFrom)) {
            sql.append("      AND :deliveryAtFrom <= dd.correction_at");
        }
        if (!Objects.isNull(deliveryAtDateFrom)) {
            sql.append("      AND :deliveryAtDateFrom <= dd.correction_at");
        }
        if (!Objects.isNull(deliveryAtTo)) {
            sql.append("      AND dd.correction_at <= :deliveryAtTo");
        }
        if (!Objects.isNull(deliveryAtDateTo)) {
            sql.append("      AND dd.correction_at <= :deliveryAtDateTo");
        }
        // 納期遅延のみ取得
        if (isDeliveryAtLate) {
            sql.append("      AND o.product_delivery_at < dd.correction_at");
            sql.append("      AND (d.delivery_date_change_reason_id IN ('2', '9')");
            sql.append("           OR d.delivery_date_change_reason_id IS NULL)");
        }
        sql.append("   INNER JOIN t_item i");
        sql.append("           ON o.part_no_id = i.id");
        sql.append("          AND i.deleted_at IS NULL");
        sql.append("   LEFT OUTER JOIN m_sirmst sm");
        sql.append("           ON o.mdf_maker_code = sm.sire");
        sql.append("          AND sm.deleted_at IS NULL");
        sql.append("          AND sm.mntflg IN ('1', '2', '')");
        sql.append(" WHERE o.deleted_at IS NULL");
        sql.append(" AND o.id = :orderId");
        sql.append(DelischeConstants.DELIVERY_REQUEST_GROUP_BY_PHRASE_SQL);
        sql.append(" ORDER BY delivery_at DESC");
        sql.append("        , delivery_id DESC");
        sql.append("        , delivery_count ASC");
    }
}

