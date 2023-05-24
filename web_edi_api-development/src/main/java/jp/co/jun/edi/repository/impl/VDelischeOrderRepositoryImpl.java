package jp.co.jun.edi.repository.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import jp.co.jun.edi.entity.VDelischeOrderEntity;
import jp.co.jun.edi.model.DelischeOrderSearchConditionModel;
import jp.co.jun.edi.repository.constants.DelischeConstants;
import jp.co.jun.edi.repository.custom.VDelischeOrderRepositoryCustom;
import jp.co.jun.edi.type.CompleteOrderType;
import jp.co.jun.edi.util.DateUtils;

/**
 * デリスケ発注Repository実装クラス.
 */
public class VDelischeOrderRepositoryImpl implements VDelischeOrderRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    private static final String SELECT_PHRASE_SQL =
            "  SELECT o.id as id"
            + "     , o.order_number as order_number"
         // PRD_0146 #10776 add JFE start
            + "     , o.relation_number as relation_number"
            + "     , o.expense_item as expense_item"
         // PRD_0146 #10776 add JFE end
            + "     , o.product_delivery_at as product_delivery_at"
            + "     , i.brand_code as brand_code"
            + "     , i.item_code as item_code"
            + "     , i.part_no as part_no"
            + "     , i.product_name as product_name"
            + "     , o.mdf_maker_code as mdf_maker_code"
            + "     , sm.name as mdf_maker_name"
            + "     , o.product_order_at as product_order_at"
            + "     , o.quantity as quantity"
            + "     , o.retail_price as retail_price"
            + "     , o.product_cost as product_cost"
            + "     , o.product_complete_order as product_complete_order"
            + "     , o.order_approve_status as order_approve_status"
            + "     , i.quality_composition_status as quality_composition_status"
            + "     , i.quality_coo_status as quality_coo_status"
            + "     , i.quality_harmful_status as quality_harmful_status";

    private static final String CHILD_EXISTS_SUB_QUERY =
            "       ("
            + "      SELECT COUNT(d.order_id)"
            + "        FROM t_delivery d"
            + "       WHERE d.order_id = o.id"
            + "         AND d.deleted_at IS NULL"
            + "     ) as child_exists";

    private static final String ORDER_BY_PHRASE_SQL =
            "  ORDER BY product_delivery_at DESC"
            + "       , brand_code DESC"
            + "       , item_code DESC"
            + "       , id DESC";

    /**
     * デリスケ発注情報を取得する.
     *
     * <pre>
     * {@code
     * SELECT o.id as id
     *      , o.order_number as order_number
     *      , o.product_delivery_at as product_delivery_at
     *      , i.brand_code as brand_code
     *      , i.item_code as item_code
     *      , i.part_no as part_no
     *      , i.product_name as product_name
     *      , o.mdf_maker_code as mdf_maker_code
     *      , sm.name as mdf_maker_name
     *      , o.product_order_at as product_order_at
     *      , o.quantity as quantity
     *      , o.retail_price
     *      , o.product_cost
     *      , o.product_complete_order as product_complete_order
     *      , o.order_approve_status as order_approve_status
     *      , i.quality_composition_status as quality_composition_status
     *      , i.quality_coo_status as quality_coo_status
     *      , i.quality_harmful_status as quality_harmful_status
     *      , CASE i.sub_season_code
     *             WHEN 1 THEN 'A1'
     *             WHEN 2 THEN 'A2'
     *             WHEN 5 THEN 'B1'
     *             WHEN 6 THEN 'B2'
     *             WHEN 9 THEN 'C'
     *             ELSE NULL
     *         END as season
     *      , CASE
     *             WHEN ps.sample_completion_at < ps.sample_completion_fix_at
     *               OR ps.specification_at < ps.specification_fix_at
     *               OR ps.texture_arrival_at < ps.texture_arrival_fix_at
     *               OR ps.attachment_arrival_at < ps.attachment_arrival_fix_at
     *               OR ps.completion_at < ps.completion_fix_at
     *               OR ps.sew_inspection_at < ps.sew_inspection_fix_at
     *               OR ps.inspection_at < ps.inspection_fix_at
     *               OR ps.leave_port_at < ps.leave_port_fix_at
     *               OR ps.enter_port_at < ps.enter_port_fix_at
     *               OR ps.customs_clearance_at < ps.customs_clearance_fix_at
     *               OR ps.dista_arrival_at < ps.dista_arrival_fix_at
     *             THEN '1'
     *             WHEN ps.sample_completion_at IS NULL
     *              AND ps.sample_completion_fix_at IS NULL
     *              AND ps.specification_at IS NULL
     *              AND ps.specification_fix_at IS NULL
     *              AND ps.texture_arrival_at IS NULL
     *              AND ps.texture_arrival_fix_at IS NULL
     *              AND ps.attachment_arrival_at IS NULL
     *              AND ps.attachment_arrival_fix_at IS NULL
     *              AND ps.completion_at IS NULL
     *              AND ps.completion_fix_at IS NULL
     *              AND ps.sew_inspection_at IS NULL
     *              AND ps.sew_inspection_fix_at IS NULL
     *              AND ps.inspection_at IS NULL
     *              AND ps.inspection_fix_at IS NULL
     *              AND ps.leave_port_at IS NULL
     *              AND ps.leave_port_fix_at IS NULL
     *              AND ps.enter_port_at IS NULL
     *              AND ps.enter_port_fix_at IS NULL
     *              AND ps.customs_clearance_at IS NULL
     *              AND ps.customs_clearance_fix_at IS NULL
     *              AND ps.dista_arrival_at IS NULL
     *              AND ps.dista_arrival_fix_at IS NULL
     *             THEN '2'
     *             ELSE '0'
     *        END as production_status
     *      , CASE WHEN DATE_FORMAT(o.product_delivery_at, '%e') >= 21
     *             THEN CASE DATE_FORMAT(o.product_delivery_at, '%c')
     *                  WHEN 12 THEN 1
     *                  ELSE DATE_FORMAT(o.product_delivery_at, '%c') + 1 END
     *             ELSE DATE_FORMAT(o.product_delivery_at, '%c')
     *        END as product_delivery_at_monthly
     *      , (
     *         SELECT COUNT(d.id)
     *         FROM t_delivery d
     *           INNER JOIN t_delivery_detail dd
     *              ON dd.delivery_id = d.id
     *             AND dd.deleted_at IS NULL
     *         WHERE d.order_id = o.id
     *           AND o.product_delivery_at < dd.correction_at
     *           AND d.deleted_at IS NULL
     *           AND (d.delivery_date_change_reason_id IN ('2', '9')
     *                OR d.delivery_date_change_reason_id IS NULL
     *               )
     *        ) as late_delivery_at_cnt
     *      , (
     *         SELECT SUM(ds.delivery_lot) as delivery_lot_sum
     *           FROM t_delivery d
     *             INNER JOIN t_delivery_detail dd
     *                     ON dd.delivery_id = d.id
     *                    AND dd.deleted_at IS NULL
     *             INNER JOIN t_delivery_sku ds
     *                     ON ds.delivery_detail_id = dd.id
     *                    AND ds.deleted_at IS NULL
     *         WHERE d.order_id = o.id
     *           AND d.deleted_at IS NULL
     *        ) as delivery_lot_sum
     *      , (
     *         SELECT SUM(ds.arrival_lot) as arrival_lot_sum
     *           FROM t_delivery d
     *             INNER JOIN t_delivery_detail dd
     *                     ON dd.delivery_id = d.id
     *                    AND dd.deleted_at IS NULL
     *             INNER JOIN t_delivery_sku ds
     *                     ON ds.delivery_detail_id = dd.id
     *                    AND ds.deleted_at IS NULL
     *         WHERE d.order_id = o.id
     *           AND d.deleted_at IS NULL
     *        ) as arrival_lot_sum
     *      , (
     *        SELECT
     *          IFNULL(SUM(tps.quantity), 0)
     *        FROM
     *          t_pos_order tps
     *        WHERE
     *          tps.part_no = i.part_no
     *          AND tps.deleted_at IS NULL
     *        ) as pos_sales_quantity
     *      , (
     *         SELECT COUNT(d.order_id)
     *           FROM t_delivery d
     *          WHERE d.order_id = o.id
     *            AND d.deleted_at IS NULL
     *        ) as child_exists
     * FROM t_order o
     *   INNER JOIN t_item i
     *           ON o.part_no_id = i.id
     *          AND i.deleted_at IS NULL
     *          AND i.brand_code IN (:brandCodeListFromDivision)
     *          AND i.brand_code IN (:brandCode)
     *          AND i.item_code IN (:itemCode)
     *          AND i.part_no LIKE :partNo
     *          AND i.sub_season_code IN (:season)
     *          AND i.year IN (:year)
     *   INNER JOIN m_sirmst sm                    ※メーカーの検索がない場合はLEFT OUTER JOIN
     *           ON o.mdf_maker_code = sm.sire
     *          AND sm.deleted_at IS NULL
     *          AND sm.mntflg IN ('1', '2', '')
     *          AND (
     *               sm.sire LIKE :mdfMaker
     *               OR sm.name LIKE :mdfMaker
     *              )
     *   INNER JOIN m_codmst c                     ※製造担当の検索がない場合はLEFT OUTER
     *           ON c.code1 = o.mdf_staff_code
     *          AND c.tblid = '22'
     *          AND c.item5 = '1'
     *          AND c.deleted_at IS NULL
     *          AND c.mntflg IN ('1', '2', '')
     *          AND (
     *               o.mdf_staff_code LIKE :mdfStaff
     *               OR c.item2 LIKE :mdfStaff
     *              )
     *   LEFT OUTER JOIN t_production_status ps
     *           ON o.id = ps.order_id
     *          AND ps.deleted_at IS NULL
     * WHERE o.deleted_at IS NULL
     *   AND :productDeliveryAtFrom <= o.product_delivery_at
     *   AND :productDeliveryAtDateFrom <= o.product_delivery_at
     *   AND o.product_delivery_at <= :productDeliveryAtTo
     *   AND o.product_delivery_at <= :productDeliveryAtDateTo
     *   AND (
     *        o.product_complete_order = '0'
     *        OR o.product_complete_order IS NULL
     *       )
     *   AND  o.id IN (
     *                 SELECT DISTINCT o.id
     *                   FROM t_order o
     *                     INNER JOIN t_delivery d
     *                             ON o.id = d.order_id
     *                            AND d.deleted_at IS NULL
     *                     INNER JOIN t_delivery_detail dd
     *                             ON d.id = dd.delivery_id
     *                            AND dd.deleted_at IS NULL
     *                            AND :deliveryAtFrom <= dd.correction_at
     *                            AND :deliveryAtDateFrom <= dd.correction_at
     *                            AND dd.correction_at <= :deliveryAtTo
     *                            AND dd.correction_at <= :deliveryAtDateTo
     *                            AND (
     *                                 d.delivery_date_change_reason_id IN ('2', '9')
     *                                 OR d.delivery_date_change_reason_id IS NULL
     *                                )
     *                  WHERE o.deleted_at IS NULL
     *                )
     *   AND o.quantity > COALESCE((
     *         SELECT SUM(ds.arrival_lot) as arrival_lot_sum
     *           FROM t_delivery d
     *             INNER JOIN t_delivery_detail dd
     *                     ON dd.delivery_id = d.id
     *                    AND dd.deleted_at IS NULL
     *             INNER JOIN t_delivery_sku ds
     *                     ON ds.delivery_detail_id = dd.id
     *                    AND ds.deleted_at IS NULL
     *          WHERE d.order_id = o.id
     *            AND d.deleted_at IS NULL
     *       ) ,0)
     * ORDER BY product_delivery_at DESC, brand_code DESC,
     *          item_code DESC, id DESC
     * }
     * </pre>
     */
    @Override
    public Page<VDelischeOrderEntity> findBySpec(final DelischeOrderSearchConditionModel searchCondition) {

        // 検索
        final StringBuilder searchSql = new StringBuilder();
        generateSelectPhrase(searchSql);
        final StringBuilder fromPhraseSql = new StringBuilder();
        generateFromPhrase(searchCondition, fromPhraseSql);
        searchSql.append(fromPhraseSql);
        final StringBuilder wherePhraseSql = new StringBuilder();
        generateWherePhrase(searchCondition, wherePhraseSql);
        searchSql.append(wherePhraseSql);
        searchSql.append(ORDER_BY_PHRASE_SQL);

        final Query serachQuery = entityManager.createNativeQuery(searchSql.toString(), VDelischeOrderEntity.class);
        setQueryParameters(searchSql, searchCondition, serachQuery);

        int pageIdx = searchCondition.getPage();
        int maxResults = searchCondition.getMaxResults();
        serachQuery.setFirstResult(pageIdx * maxResults);
        serachQuery.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        List<VDelischeOrderEntity> rslt = serachQuery.getResultList();

        // 件数取得
        Integer total = countBySpec(searchCondition, fromPhraseSql, wherePhraseSql);

        final PageRequest pageRequest = PageRequest.of(searchCondition.getPage(), searchCondition.getMaxResults());
        return new PageImpl<>(rslt, pageRequest, total);
    }

    /**
     * クエリにパラメータを設定する.
     * @param sql SQL文
     * @param searchCondition 検索条件
     * @param query クエリ
     */
    private void setQueryParameters(final StringBuilder sql, final DelischeOrderSearchConditionModel searchCondition,
            final Query query) {
        List<String> brandCodeListFromDivision = searchCondition.getBrandCodeListFromDivision();
        final List<String> brandCodeList = jp.co.jun.edi.util.StringUtils.splitWhitespace(searchCondition.getBrandCode());
        final List<String> itemCodeList = jp.co.jun.edi.util.StringUtils.splitWhitespace(searchCondition.getItemCode());
        final String partNo = searchCondition.getPartNo();
        final List<String> season = searchCondition.getSeason();
        final String mdfMaker = searchCondition.getMdfMaker();
        final String mdfStaff = searchCondition.getMdfStaff();
        final Date productDeliveryAtFrom = searchCondition.getProductDeliveryAtFrom();
        final Date productDeliveryAtFromByMonthly = searchCondition.getProductDeliveryAtFromByMonthly();
        final Date productDeliveryAtTo = searchCondition.getProductDeliveryAtTo();
        final Date productDeliveryAtToByMonthly = searchCondition.getProductDeliveryAtToByMonthly();
        final Date deliveryAtFrom = searchCondition.getDeliveryAtFrom();
        final Date deliveryAtFromByMdweek = searchCondition.getDeliveryAtFromByMdweek();
        final Date deliveryAtTo = searchCondition.getDeliveryAtTo();
        final Date deliveryAtToByMdweek = searchCondition.getDeliveryAtToByMdweek();
        // PRD_0146 #10776 add JFE start
        final String expenseItem = searchCondition.getExpenseItem();
        // PRD_0146 #10776 add JFE end

        if (!StringUtils.isEmpty(searchCondition.getDivisionCode())) {
            if (brandCodeListFromDivision.isEmpty()) {
                brandCodeListFromDivision = new ArrayList<>(1);
                brandCodeListFromDivision.add("");
            }
            query.setParameter("brandCodeListFromDivision", brandCodeListFromDivision);
        }
        if (!brandCodeList.isEmpty()) {
            query.setParameter("brandCode", brandCodeList);
        }
        if (!itemCodeList.isEmpty()) {
            query.setParameter("itemCode", itemCodeList);
        }
        if (!StringUtils.isEmpty(partNo)) {
            query.setParameter("partNo", "%" + partNo + "%");
        }
        if (!Objects.isNull(season) && season.size() > 0) {
            query.setParameter("season", season);
        }
        if (!StringUtils.isEmpty(mdfMaker)) {
            query.setParameter("mdfMaker", "%" + mdfMaker + "%");
        }
        if (!StringUtils.isEmpty(mdfStaff)) {
            query.setParameter("mdfStaff", "%" + mdfStaff + "%");
        }
        if (!Objects.isNull(productDeliveryAtFrom)) {
            query.setParameter("productDeliveryAtFrom", DateUtils.truncateDate(productDeliveryAtFrom));
        }
        if (!Objects.isNull(productDeliveryAtFromByMonthly)) {
            query.setParameter("productDeliveryAtFromByMonthly", DateUtils.truncateDate(productDeliveryAtFromByMonthly));
        }
        if (!Objects.isNull(productDeliveryAtTo)) {
            query.setParameter("productDeliveryAtTo", DateUtils.truncateDate(productDeliveryAtTo));
        }
        if (!Objects.isNull(productDeliveryAtToByMonthly)) {
            query.setParameter("productDeliveryAtToByMonthly", DateUtils.truncateDate(productDeliveryAtToByMonthly));
        }
        if (!Objects.isNull(deliveryAtFrom)) {
            query.setParameter("deliveryAtFrom", DateUtils.truncateDate(deliveryAtFrom));
        }
        if (!Objects.isNull(deliveryAtFromByMdweek)) {
            query.setParameter("deliveryAtFromByMdweek", DateUtils.truncateDate(deliveryAtFromByMdweek));
        }
        if (!Objects.isNull(deliveryAtTo)) {
            query.setParameter("deliveryAtTo", DateUtils.truncateDate(deliveryAtTo));
        }
        if (!Objects.isNull(deliveryAtToByMdweek)) {
            query.setParameter("deliveryAtToByMdweek", DateUtils.truncateDate(deliveryAtToByMdweek));
        }
        // PRD_0146 #10776 add JFE start
        if (!StringUtils.isEmpty(expenseItem)) {
            query.setParameter("expenseItem", expenseItem);
        }
        // PRD_0146 #10776 add JFE end
    }

    /**
     * SELECT句を作成する.
     * @param sql sql
     */
    private void generateSelectPhrase(final StringBuilder sql) {
        sql.append(SELECT_PHRASE_SQL);
        sql.append(" ,");
        sql.append(DelischeConstants.SEASON_SQL);
        sql.append(" as season,");
        sql.append(DelischeConstants.PRODUCTION_STATUS_SQL);
        sql.append(" as production_status,");
        sql.append(DelischeConstants.PRODUCT_DELIVERY_AT_MONTHLY_SQL);
        sql.append(" as product_delivery_at_monthly,");
        sql.append(DelischeConstants.LATE_DELIVERY_AT_CNT_SUB_QUERY);
        sql.append(" as late_delivery_at_cnt,");
        sql.append(DelischeConstants.DELIVERY_LOT_ORDER_SUM_SUB_QUERY);
        sql.append(" as delivery_lot_sum,");
        sql.append(DelischeConstants.ARRIVAL_LOT_ORDER_SUM_SUB_QUERY);
        sql.append(" as arrival_lot_sum,");
        sql.append(DelischeConstants.POS_SALES_QUANTITY_SUM_SUB_QUERY);
        sql.append(" as pos_sales_quantity,");
        sql.append(CHILD_EXISTS_SUB_QUERY);
    }

    /**
     * FROM句を作成する.
     * @param searchCondition デリスケ発注検索条件
     * @param sql sql
     */
    private void generateFromPhrase(final DelischeOrderSearchConditionModel searchCondition, final StringBuilder sql) {
        final List<String> brandCodeList = jp.co.jun.edi.util.StringUtils.splitWhitespace(searchCondition.getBrandCode());
        final List<String> itemCodeList = jp.co.jun.edi.util.StringUtils.splitWhitespace(searchCondition.getItemCode());
        final String partNo = searchCondition.getPartNo();
        final List<String> season = searchCondition.getSeason();
        final String mdfMaker = searchCondition.getMdfMaker();
        final String mdfStaff = searchCondition.getMdfStaff();

        sql.append(" FROM t_order o");
        sql.append("   INNER JOIN t_item i");
        sql.append("           ON o.part_no_id = i.id");
        sql.append("          AND i.deleted_at IS NULL");
        if (!StringUtils.isEmpty(searchCondition.getDivisionCode())) {
            sql.append("      AND i.brand_code IN (:brandCodeListFromDivision)");
        }
        if (!brandCodeList.isEmpty()) {
            sql.append("      AND i.brand_code IN (:brandCode)");
        }
        if (!itemCodeList.isEmpty()) {
            sql.append("      AND i.item_code IN (:itemCode)");
        }
        if (!StringUtils.isEmpty(partNo)) {
            sql.append("      AND i.part_no LIKE :partNo");
        }
        if (!Objects.isNull(season) && season.size() > 0) {
            sql.append("      AND i.sub_season_code IN (:season)");
        }
        if (!StringUtils.isEmpty(mdfMaker)) {
            sql.append(" INNER JOIN m_sirmst sm");
        } else {
            sql.append(" LEFT OUTER JOIN m_sirmst sm");
        }
        sql.append("           ON o.mdf_maker_code = sm.sire");
        sql.append("          AND sm.deleted_at IS NULL");
        sql.append("          AND sm.mntflg IN ('1', '2', '')");
        if (!StringUtils.isEmpty(mdfMaker)) {
            sql.append("      AND (sm.sire LIKE :mdfMaker");
            sql.append("           OR sm.name LIKE :mdfMaker)");
        }

        if (!StringUtils.isEmpty(mdfStaff)) {
            sql.append(" INNER JOIN m_codmst c");
        } else {
            sql.append(" LEFT OUTER JOIN m_codmst c");
        }
        sql.append("           ON c.code1 = o.mdf_staff_code");
        sql.append("          AND c.tblid = '22'");
        sql.append("          AND c.item5 = '1'");
        sql.append("          AND c.deleted_at IS NULL");
        sql.append("          AND c.mntflg IN ('1', '2', '')");
        if (!StringUtils.isEmpty(mdfStaff)) {
            sql.append("      AND (o.mdf_staff_code LIKE :mdfStaff");
            sql.append("           OR c.item2 LIKE :mdfStaff)");
        }

        sql.append("   LEFT OUTER JOIN t_production_status ps");
        sql.append("           ON o.id = ps.order_id");
        sql.append("          AND ps.deleted_at IS NULL");
    }

    /**
     * WHERE句を作成する.
     * @param searchCondition デリスケ発注検索条件
     * @param sql sql
     */
    private void generateWherePhrase(final DelischeOrderSearchConditionModel searchCondition, final StringBuilder sql) {
        final Date productDeliveryAtFrom = searchCondition.getProductDeliveryAtFrom();
        final Date productDeliveryAtFromByMonthly = searchCondition.getProductDeliveryAtFromByMonthly();
        final Date productDeliveryAtTo = searchCondition.getProductDeliveryAtTo();
        final Date productDeliveryAtToByMonthly = searchCondition.getProductDeliveryAtToByMonthly();
        final Date deliveryAtFrom = searchCondition.getDeliveryAtFrom();
        final Date deliveryAtFromByMdweek = searchCondition.getDeliveryAtFromByMdweek();
        final Date deliveryAtTo = searchCondition.getDeliveryAtTo();
        final Date deliveryAtToByMdweek = searchCondition.getDeliveryAtToByMdweek();
        final boolean isDeliveryAtLate = searchCondition.isDeliveryAtLateFlg();
        final boolean isExcludeCompleteOrder = searchCondition.isExcludeCompleteOrder();
        final boolean isExistsOrderRemaining = searchCondition.isExistsOrderRemaining();
        // PRD_0146 #10776 add JFE start
        final String expenseItem = searchCondition.getExpenseItem();
        // PRD_0146 #10776 add JFE end

        sql.append(" WHERE o.deleted_at IS NULL");
        if (!Objects.isNull(productDeliveryAtFrom)) {
            sql.append(" AND :productDeliveryAtFrom <= o.product_delivery_at");
        }
        if (!Objects.isNull(productDeliveryAtFromByMonthly)) {
            sql.append(" AND :productDeliveryAtFromByMonthly <= o.product_delivery_at");
        }
        if (!Objects.isNull(productDeliveryAtTo)) {
            sql.append(" AND o.product_delivery_at <= :productDeliveryAtTo");
        }
        if (!Objects.isNull(productDeliveryAtToByMonthly)) {
            sql.append(" AND o.product_delivery_at <= :productDeliveryAtToByMonthly");
        }
        // PRD_0146 #10776 add JFE start
        if (!StringUtils.isEmpty(expenseItem)) {
            sql.append(" AND o.expense_item = :expenseItem");
        }
        // PRD_0146 #10776 add JFE end
        // 完納は除く
        if (isExcludeCompleteOrder) {
            sql.append(" AND (o.product_complete_order = " + CompleteOrderType.INCOMPLETE.getValue());
            sql.append("      OR o.product_complete_order IS NULL)");
        }

        // 納期
        if (!Objects.isNull(deliveryAtFrom) || !Objects.isNull(deliveryAtTo)
                || !Objects.isNull(deliveryAtFromByMdweek) || !Objects.isNull(deliveryAtToByMdweek)
                || isDeliveryAtLate) {
            sql.append(" AND o.id IN (");
            sql.append("              SELECT DISTINCT o.id");
            sql.append("                FROM t_order o");
            sql.append("                  INNER JOIN t_delivery d");
            sql.append("                          ON o.id = d.order_id");
            sql.append("                         AND d.deleted_at IS NULL");
            sql.append("                  INNER JOIN t_delivery_detail dd");
            sql.append("                          ON d.id = dd.delivery_id");
            sql.append("                         AND dd.deleted_at IS NULL");
            if (!Objects.isNull(deliveryAtFrom)) {
                sql.append("                     AND :deliveryAtFrom <= dd.correction_at");
            }
            if (!Objects.isNull(deliveryAtFromByMdweek)) {
                sql.append("                     AND :deliveryAtFromByMdweek <= dd.correction_at");
            }
            if (!Objects.isNull(deliveryAtTo)) {
                sql.append("                     AND dd.correction_at <= :deliveryAtTo");
            }
            if (!Objects.isNull(deliveryAtToByMdweek)) {
                sql.append("                     AND dd.correction_at <= :deliveryAtToByMdweek");
            }
            // 納期遅延のみ取得
            if (isDeliveryAtLate) {
                sql.append("                     AND o.product_delivery_at < dd.correction_at");
                sql.append("                     AND (d.delivery_date_change_reason_id IN ('2', '9')");
                sql.append("                          OR d.delivery_date_change_reason_id IS NULL)");
            }
            sql.append("              WHERE o.deleted_at IS NULL");
            sql.append(" )");
        }

        // 発注残があるもの
        if (isExistsOrderRemaining) {
            sql.append(" AND o.quantity > COALESCE(");
            sql.append(DelischeConstants.ARRIVAL_LOT_ORDER_SUM_SUB_QUERY);
            sql.append(" ,0)");
        }
    }

    @Override
    public int countBySpec(final DelischeOrderSearchConditionModel searchCondition,
            final StringBuilder fromPhraseSqlParam, final StringBuilder wherePhraseSqlParam) {
        final StringBuilder cntSql = new StringBuilder();
        cntSql.append(DelischeConstants.SELECT_CNT_PHRASE_SQL);

        StringBuilder fromPhraseSql = fromPhraseSqlParam;
        if (fromPhraseSqlParam == null) {
            fromPhraseSql = new StringBuilder();
            generateFromPhrase(searchCondition, fromPhraseSql);
        }
        cntSql.append(fromPhraseSql);

        StringBuilder wherePhraseSql = wherePhraseSqlParam;
        if (wherePhraseSqlParam == null) {
            wherePhraseSql = new StringBuilder();
            generateFromPhrase(searchCondition, wherePhraseSql);
        }
        cntSql.append(wherePhraseSql);

        final Query cntQuery = entityManager.createNativeQuery(cntSql.toString());
        setQueryParameters(cntSql, searchCondition, cntQuery);
        return ((Number) cntQuery.getSingleResult()).intValue();
    }
}
