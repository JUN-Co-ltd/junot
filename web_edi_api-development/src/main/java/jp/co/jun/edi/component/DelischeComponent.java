package jp.co.jun.edi.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jp.co.jun.edi.entity.VDelischeDeliveryRequestEntity;
import jp.co.jun.edi.entity.VDelischeDeliverySkuEntity;
import jp.co.jun.edi.entity.VDelischeOrderEntity;
import jp.co.jun.edi.entity.key.VDelischeDeliveryRequestKey;
import jp.co.jun.edi.model.DelischeDeliveryRequestSearchConditionModel;
import jp.co.jun.edi.model.DelischeDeliverySkuSearchConditionModel;
import jp.co.jun.edi.model.DelischeOrderSearchConditionModel;
import jp.co.jun.edi.model.VDelischeDeliveryRequestModel;
import jp.co.jun.edi.model.VDelischeDeliverySkuModel;
import jp.co.jun.edi.model.VDelischeOrderModel;
import jp.co.jun.edi.repository.VDelischeDeliveryRequestRepository;
import jp.co.jun.edi.repository.VDelischeDeliverySkuRepository;
import jp.co.jun.edi.repository.constants.DelischeConstants;
import jp.co.jun.edi.type.CompleteOrderType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.NumberUtils;

/**
 * デリスケ関連のコンポーネント.
 */
@Component
public class DelischeComponent extends GenericComponent {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private VDelischeDeliveryRequestRepository vDelischeDeliveryRequestRepository;

    @Autowired
    private VDelischeDeliverySkuRepository vDelischeDeliverySkuRepository;

    private static final int ROUND_HALF_UP_THREE = 3;
    private static final int HUNDRED = 100;

    // デリスケCSV SELECT句
    private static final String CSV_SELECT_PHRASE_SQL =
            "  SELECT o.id as order_id"
            + "     , IFNULL(d.id, 0) as delivery_id"
            + "     , IFNULL(d.delivery_count, 0) as delivery_count"
            + "     , IFNULL(ds.color_code, '') as color_code"
            + "     , IFNULL(ds.size, '') as size"
            + "     , IFNULL(dd.correction_at, '1900-01-01') as delivery_at"
            + "     , o.product_delivery_at as product_delivery_at"
            + "     , i.brand_code as brand_code"
            + "     , i.item_code as item_code"
            + "     , i.part_no as part_no"
            + "     , i.product_name as product_name"
            + "     , CAST(szm.jun as SIGNED) as jun"
            + "     , sm.name as mdf_maker_name"
            + "     , o.product_order_at as product_order_at"
            + "     , o.quantity as quantity"
            + "     , os.product_order_lot as product_order_lot"
            + "     , o.retail_price as retail_price"
            + "     , o.product_cost as product_cost"
            + "     , SUM(ds.delivery_lot) as delivery_lot"
            + "     , SUM(ds.arrival_lot) as arrival_lot";

    /** デリスケCSV GROUP BY句. */
    private static final String CSV_GROUP_BY_PHRASE_SQL =
            "  GROUP BY o.id"
            + "    , d.id"
            + "    , ds.color_code"
            + "    , ds.size";

    /** デリスケCSV ORDER BY句. */
    private static final String CSV_ORDER_BY_PHRASE_SQL =
            "  ORDER BY order_id ASC"
            + "       , delivery_id ASC"
            + "       , delivery_count ASC"
            + "       , delivery_at ASC"
            + "       , color_code ASC"
            + "       , jun ASC";

    /**
     * デリスケ発注リストを取得する.
     *
     * @param pageVDelischeOrder デリスケ発注情報
     * @return デリスケ納品依頼リスト
     */
    public List<VDelischeOrderModel> listDelischeOrder(final Page<VDelischeOrderEntity> pageVDelischeOrder) {
        final List<VDelischeOrderModel> vDelischeOrderModelList = new ArrayList<>();

        for (final VDelischeOrderEntity delischeOrderEntity : pageVDelischeOrder) {
            final VDelischeOrderModel delischeOrderModel = new VDelischeOrderModel();
            BeanUtils.copyProperties(delischeOrderEntity, delischeOrderModel);

            // 計算
            delischeOrderModel.setOrderRemainingLot(calculateOrderRemainingLot(delischeOrderEntity.getQuantity(),
                    delischeOrderEntity.getArrivalLotSum()));
            final int netSalesQuantity = delischeOrderEntity.getPosSalesQuantity();
            delischeOrderModel.setNetSalesQuantity(netSalesQuantity);
            delischeOrderModel.setStockQuantity(calculateStockLot(
                    delischeOrderEntity.getArrivalLotSum(), netSalesQuantity));
            delischeOrderModel.setCalculateRetailPrice(calculateRetailPrice(delischeOrderEntity.getQuantity(),
                    delischeOrderEntity.getRetailPrice()));
            delischeOrderModel.setCalculateProductCost(calculateProductCost(delischeOrderEntity.getQuantity(),
                    delischeOrderEntity.getProductCost()));
            delischeOrderModel.setCostRate(calculateCostRate(delischeOrderEntity.getRetailPrice(), delischeOrderEntity.getProductCost()));

            vDelischeOrderModelList.add(delischeOrderModel); // レスポンスに返却する
        }

        return vDelischeOrderModelList;
    }

    /**
     * デリスケ納品依頼リストを取得する.
     *
     * @param searchCondition 検索条件
     * @return デリスケ納品依頼リスト
     */
    public List<VDelischeDeliveryRequestModel> listDelischeDeliveryRequest(final DelischeDeliveryRequestSearchConditionModel searchCondition) {
        final List<VDelischeDeliveryRequestModel> vDelischeDeliveryRequestModelList = new ArrayList<>();

        for (final VDelischeDeliveryRequestEntity delischeDeliveryRequestEntity : vDelischeDeliveryRequestRepository.findBySpec(
                searchCondition.getOrderId(), searchCondition.getDeliveryAtFrom(), searchCondition.getDeliveryAtTo(),
                searchCondition.getDeliveryAtFromByMdweek(), searchCondition.getDeliveryAtToByMdweek(), searchCondition.isDeliveryAtLateFlg())) {

            final VDelischeDeliveryRequestModel delischeDeliveryRequestModel = new VDelischeDeliveryRequestModel();
            BeanUtils.copyProperties(delischeDeliveryRequestEntity, delischeDeliveryRequestModel);
            final VDelischeDeliveryRequestKey delischeDeliveryRequestKey = delischeDeliveryRequestEntity.getVDelischeDeliveryRequestKey();
            delischeDeliveryRequestModel.setOrderId(delischeDeliveryRequestKey.getOrderId());
            delischeDeliveryRequestModel.setDeliveryId(delischeDeliveryRequestKey.getDeliveryId());
            delischeDeliveryRequestModel.setDeliveryCount(delischeDeliveryRequestKey.getDeliveryCount());
            delischeDeliveryRequestModel.setDeliveryAt(delischeDeliveryRequestKey.getDeliveryAt());

            // 計算
            delischeDeliveryRequestModel.setCalculateRetailPrice(calculateRetailPrice(delischeDeliveryRequestEntity.getDeliveryLotSum(),
                    delischeDeliveryRequestEntity.getRetailPrice()));
            delischeDeliveryRequestModel.setCalculateProductCost(calculateProductCost(delischeDeliveryRequestEntity.getDeliveryLotSum(),
                    delischeDeliveryRequestEntity.getProductCost()));

            // レスポンスに返却する
            vDelischeDeliveryRequestModelList.add(delischeDeliveryRequestModel);
        }

        return vDelischeDeliveryRequestModelList;
    }

    /**
     * デリスケ納品SKUリストを取得する.
     *
     * @param searchCondition 検索条件
     * @return デリスケ納品SKUリスト
     */
    public List<VDelischeDeliverySkuModel> listDelischeDeliverySku(final DelischeDeliverySkuSearchConditionModel searchCondition) {
        final List<VDelischeDeliverySkuModel> vDelischeDeliverySkuModelList = new ArrayList<>();
        final PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);

        for (final VDelischeDeliverySkuEntity delischeDeliverySkuEntity : vDelischeDeliverySkuRepository.findByDelischeDeriveryRequest(
                searchCondition.getOrderId(),
                searchCondition.getDeliveryId(),
                pageRequest)) {

            final VDelischeDeliverySkuModel delischeDeliverySkuModel = new VDelischeDeliverySkuModel();
            BeanUtils.copyProperties(delischeDeliverySkuEntity, delischeDeliverySkuModel);

            // 計算
            delischeDeliverySkuModel.setCalculateRetailPrice(calculateRetailPrice(delischeDeliverySkuEntity.getDeliveryLot(),
                    delischeDeliverySkuEntity.getRetailPrice()));
            delischeDeliverySkuModel.setCalculateProductCost(calculateProductCost(delischeDeliverySkuEntity.getDeliveryLot(),
                    delischeDeliverySkuEntity.getProductCost()));

            // レスポンスに返却する
            vDelischeDeliverySkuModelList.add(delischeDeliverySkuModel);
        }
        return vDelischeDeliverySkuModelList;
    }

    /**
     * デリスケCSV取得SQL文を作成する.
     *
     * <pre>
     * {@code
     * SELECT o.id as order_id
     *        ,IFNULL(d.id, 0) as delivery_id
     *        ,IFNULL(d.delivery_count, 0) as delivery_count
     *        ,IFNULL(ds.color_code, '') as color_code
     *        ,IFNULL(ds.size, '') as size
     *        ,IFNULL(dd.correction_at, '1900-01-01') as delivery_at
     *        ,o.product_delivery_at as product_delivery_at
     *        ,i.brand_code as brand_code
     *        ,i.item_code as item_code
     *        ,i.part_no as part_no
     *        ,i.product_name as product_name
     *        ,CAST(szm.jun as SIGNED) as jun
     *        ,sm.name as mdf_maker_name
     *        ,o.product_order_at as product_order_at
     *        ,o.quantity as quantity
     *        ,os.product_order_lot as product_order_lot
     *        ,o.retail_price as retail_price
     *        ,o.product_cost as product_cost
     *        ,SUM(ds.delivery_lot) as delivery_lot
     *        ,SUM(ds.arrival_lot) as arrival_lot
     *        ,CASE i.sub_season_code
     *              WHEN 1 THEN 'A1'
     *              WHEN 2 THEN 'A2'
     *              WHEN 5 THEN 'B1'
     *              WHEN 6 THEN 'B2'
     *              WHEN 9 THEN 'C'
     *              ELSE NULL
     *         END as season
     *        ,CASE WHEN ps.sample_completion_at < ps.sample_completion_fix_at
     *                OR ps.specification_at < ps.specification_fix_at
     *                OR ps.texture_arrival_at < ps.texture_arrival_fix_at
     *                OR ps.attachment_arrival_at < ps.attachment_arrival_fix_at
     *                OR ps.completion_at < ps.completion_fix_at
     *                OR ps.sew_inspection_at < ps.sew_inspection_fix_at
     *                OR ps.inspection_at < ps.inspection_fix_at
     *                OR ps.leave_port_at < ps.leave_port_fix_at
     *                OR ps.enter_port_at < ps.enter_port_fix_at
     *                OR ps.customs_clearance_at < ps.customs_clearance_fix_at
     *                OR ps.dista_arrival_at < ps.dista_arrival_fix_at
     *              THEN '1'
     *              WHEN ps.sample_completion_at IS NULL
     *               AND ps.sample_completion_fix_at IS NULL
     *               AND ps.specification_at IS NULL
     *               AND ps.specification_fix_at IS NULL
     *               AND ps.texture_arrival_at IS NULL
     *               AND ps.texture_arrival_fix_at IS NULL
     *               AND ps.attachment_arrival_at IS NULL
     *               AND ps.attachment_arrival_fix_at IS NULL
     *               AND ps.completion_at IS NULL
     *               AND ps.completion_fix_at IS NULL
     *               AND ps.sew_inspection_at IS NULL
     *               AND ps.sew_inspection_fix_at IS NULL
     *               AND ps.inspection_at IS NULL
     *               AND ps.inspection_fix_at IS NULL
     *               AND ps.leave_port_at IS NULL
     *               AND ps.leave_port_fix_at IS NULL
     *               AND ps.enter_port_at IS NULL
     *               AND ps.enter_port_fix_at IS NULL
     *               AND ps.customs_clearance_at IS NULL
     *               AND ps.customs_clearance_fix_at IS NULL
     *               AND ps.dista_arrival_at IS NULL
     *               AND ps.dista_arrival_fix_at IS NULL
     *              THEN '2'
     *              ELSE '0'
     *         END  as production_status
     *        ,CASE WHEN DATE_FORMAT(o.product_delivery_at, '%e') >= 21
     *              THEN CASE DATE_FORMAT(o.product_delivery_at, '%c')
     *                        WHEN 12
     *                        THEN 1
     *                        ELSE DATE_FORMAT(o.product_delivery_at, '%c') + 1
     *                   END
     *              ELSE DATE_FORMAT(o.product_delivery_at, '%c')
     *         END  as product_delivery_at_monthly
     *        ,(
     *          SELECT COUNT(d.id)
     *            FROM t_delivery d
     *              INNER JOIN t_delivery_detail dd
     *                      ON dd.delivery_id = d.id
     *                     AND dd.deleted_at IS NULL
     *          WHERE d.order_id = o.id
     *            AND d.deleted_at IS NULL
     *            AND    o.product_delivery_at < dd.correction_at
     *            AND (
     *                 d.delivery_date_change_reason_id IN ('2', '9')
     *                 OR d.delivery_date_change_reason_id IS NULL
     *                )
     *         ) as late_delivery_at_cnt
     *        ,(
     *          SELECT SUM(ds.delivery_lot) as delivery_lot_sum
     *            FROM t_delivery d
     *              INNER JOIN t_delivery_detail dd
     *                      ON dd.delivery_id = d.id
     *                     AND dd.deleted_at IS NULL
     *              INNER JOIN t_delivery_sku ds
     *                      ON ds.delivery_detail_id = dd.id
     *                     AND ds.deleted_at IS NULL
     *          WHERE d.order_id = o.id
     *            AND d.deleted_at IS NULL
     *         ) as delivery_lot_order_sum
     *        ,(
     *          SELECT SUM(ds.arrival_lot) as arrival_lot_sum
     *            FROM t_delivery d
     *              INNER JOIN t_delivery_detail dd
     *                      ON dd.delivery_id = d.id
     *                     AND dd.deleted_at IS NULL
     *              INNER JOIN t_delivery_sku ds
     *                      ON ds.delivery_detail_id = dd.id
     *                     AND ds.deleted_at IS NULL
     *          WHERE d.order_id = o.id
     *            AND d.deleted_at IS NULL
     *         ) as arrival_lot_order_sum
     *        ,(
     *          SELECT
     *            IFNULL(SUM(tps.quantity), 0)
     *          FROM
     *            t_pos_order tps
     *          WHERE
     *            tps.part_no = i.part_no
     *            AND tps.deleted_at IS NULL
     *         ) as pos_sales_quantity
     *         CASE WHEN DATE_FORMAT(dd.correction_at, '%e') >= 21
     *              THEN CASE DATE_FORMAT(dd.correction_at, '%c')
     *                        WHEN 12
     *                        THEN 1
     *                        ELSE DATE_FORMAT(dd.correction_at, '%c') + 1
     *                   END
     *              ELSE DATE_FORMAT(dd.correction_at, '%c')
     *         END as delivery_at_monthly
     *        ,(
     *          SELECT
     *            SUM(pol_os.product_order_lot)
     *          FROM
     *            t_order_sku pol_os
     *          WHERE
     *            pol_os.order_id = d.order_id
     *            AND pol_os.deleted_at IS NULL
     *            AND EXISTS (
     *              SELECT
     *                pol_ds.id
     *              FROM
     *                t_delivery_sku pol_ds
     *                INNER JOIN t_delivery_detail pol_dd
     *                  ON pol_ds.delivery_detail_id = pol_dd.id
     *                  AND pol_dd.deleted_at IS NULL
     *              WHERE
     *                pol_dd.delivery_id = d.id
     *                AND pol_ds.color_code = pol_os.color_code
     *                AND pol_ds.size = pol_os.size
     *            )
     *         ) as product_order_lot_sum
     *         , (
     *            SELECT
     *              SUM(dl_ds.delivery_lot)
     *            FROM
     *              t_delivery_sku dl_ds
     *              INNER JOIN t_delivery_detail dl_dd
     *                ON dl_ds.delivery_detail_id = dl_dd.id
     *                AND dl_dd.deleted_at IS NULL
     *            WHERE
     *              dl_dd.delivery_id = d.id
     *              AND dl_ds.deleted_at IS NULL
     *           ) as delivery_lot_sum
     *         , (
     *            SELECT
     *              SUM(al_ds.arrival_lot)
     *            FROM
     *              t_delivery_sku al_ds
     *              INNER JOIN t_delivery_detail al_dd
     *                ON al_ds.delivery_detail_id = al_dd.id
     *                AND al_dd.deleted_at IS NULL
     *            WHERE
     *              al_dd.delivery_id = d.id
     *              AND al_ds.deleted_at IS NULL
     *           ) as arrival_lot_sum
     * FROM t_order o
     *   INNER JOIN t_item i
     *           ON o.part_no_id = i.id
     *          AND i.deleted_at IS NULL
     *          AND i.brand_code IN (?)
     *          AND i.item_code IN (?)
     *          AND i.part_no LIKE ?
     *          AND i.sub_season_code IN (?)
     *   INNER JOIN m_sirmst sm
     *           ON o.mdf_maker_code = sm.sire
     *          AND sm.deleted_at IS NULL
     *          AND sm.mntflg IN ('1', '2', '')
     *          AND (sm.sire LIKE ?
     *           OR sm.name LIKE ?)
     *   INNER JOIN m_codmst c
     *           ON c.code1 = o.mdf_staff_code
     *          AND c.tblid = '22'
     *          AND c.item5 = '1'
     *          AND c.deleted_at IS NULL
     *          AND c.mntflg IN ('1', '2', '')
     *          AND (
     *               o.mdf_staff_code LIKE ?
     *               OR c.item2 LIKE ?
     *              )
     *   LEFT OUTER JOIN t_delivery d
     *           ON o.id = d.order_id
     *          AND d.deleted_at IS NULL
     *   LEFT OUTER JOIN t_delivery_detail dd
     *           ON d.id = dd.delivery_id
     *          AND d.deleted_at IS NULL
     *   LEFT OUTER JOIN t_delivery_sku ds
     *           ON dd.id = ds.delivery_detail_id
     *          AND ds.deleted_at IS NULL
     *   LEFT OUTER JOIN t_order_sku os
     *           ON d.order_id = os.order_id
     *          AND os.deleted_at IS NULL
     *          AND ds.color_code = os.color_code
     *          AND ds.size = os.size
     *   LEFT OUTER OUTER JOIN m_sizmst szm
     *           ON szm.hscd = CONCAT(i.brand_code, i.item_code)
     *          AND szm.szkg =  ds.size
     *          AND szm.deleted_at IS NULL
     *          AND szm.mntflg IN ('1', '2', '')
     * WHERE o.deleted_at IS NULL
     *   AND ? <= o.product_delivery_at
     *   AND ? <= o.product_delivery_at
     *   AND o.product_delivery_at <= ?
     *   AND o.product_delivery_at <= ?
     *   AND (
     *        o.product_complete_order = 0
     *        OR o.product_complete_order IS NULL
     *       )
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
     * GROUP BY o.id
     *         ,d.id
     *         ,ds.color_code
     *         ,ds.size
     * ORDER BY order_id ASC
     *         ,delivery_id ASC
     *         ,delivery_count ASC
     *         ,delivery_at ASC
     *         ,color_code ASC
     *         ,jun ASC
     * }
     * </pre>
     *
     * @param searchCondition デリスケ発注検索条件
     * @param sql sql
     */
    public void generateDelischeCsvSql(final DelischeOrderSearchConditionModel searchCondition, final StringBuilder sql) {
        generateDelischeCsvSelectPhrase(sql);
        generateDelischeCsvFromPhrase(searchCondition, sql);
        generateDelischeCsvWherePhrase(searchCondition, sql);
        sql.append(CSV_GROUP_BY_PHRASE_SQL);
        sql.append(CSV_ORDER_BY_PHRASE_SQL);
    }

    /**
     * デリスケCSV取得のSELECT句を作成する.
     * @param sql sql
     */
    private void generateDelischeCsvSelectPhrase(final StringBuilder sql) {
        sql.append(CSV_SELECT_PHRASE_SQL);
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
        sql.append(" as delivery_lot_order_sum,");
        sql.append(DelischeConstants.ARRIVAL_LOT_ORDER_SUM_SUB_QUERY);
        sql.append(" as arrival_lot_order_sum,");
        sql.append(DelischeConstants.POS_SALES_QUANTITY_SUM_SUB_QUERY);
        sql.append(" as pos_sales_quantity,");
        sql.append(DelischeConstants.DELIVERY_AT_MONTHLY_SQL);
        sql.append(" as delivery_at_monthly,");
        sql.append(DelischeConstants.PRODUCT_ORDER_LOT_SUM_SUB_QUERY);
        sql.append(" as product_order_lot_sum,");
        sql.append(DelischeConstants.DELIVERY_LOT_SUM_SUB_QUERY);
        sql.append(" as delivery_lot_sum,");
        sql.append(DelischeConstants.ARRIVAL_LOT_SUM_SUB_QUERY);
        sql.append(" as arrival_lot_sum");
    }

    /**
     * デリスケCSV取得のFROM句を作成する.
     * @param searchCondition デリスケ発注検索条件
     * @param sql sql
     */
    public void generateDelischeCsvFromPhrase(final DelischeOrderSearchConditionModel searchCondition, final StringBuilder sql) {
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

        generateDeliverySkuFromPhrase(searchCondition, sql);
    }

    /**
     * デリスケCSV取得のWHERE句を作成する.
     * @param searchCondition デリスケ発注検索条件
     * @param sql sql
     */
    public void generateDelischeCsvWherePhrase(final DelischeOrderSearchConditionModel searchCondition, final StringBuilder sql) {
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
        // 完納は除く
        if (isExcludeCompleteOrder) {
            sql.append(" AND (o.product_complete_order = " + CompleteOrderType.INCOMPLETE.getValue());
            sql.append("      OR o.product_complete_order IS NULL)");
        }

        // 納期
        if (!Objects.isNull(deliveryAtFrom)) {
            sql.append("      AND :deliveryAtFrom <= dd.correction_at");
        }
        if (!Objects.isNull(deliveryAtFromByMdweek)) {
            sql.append("      AND :deliveryAtFromByMdweek <= dd.correction_at");
        }
        if (!Objects.isNull(deliveryAtTo)) {
            sql.append("      AND dd.correction_at <= :deliveryAtTo");
        }
        if (!Objects.isNull(deliveryAtToByMdweek)) {
            sql.append("      AND dd.correction_at <= :deliveryAtToByMdweek");
        }

        if (isDeliveryAtLate) {
            sql.append("      AND ");
            sql.append(DelischeConstants.LATE_DELIVERY_AT_SQL);
        }

        // 発注残があるもの
        if (isExistsOrderRemaining) {
            sql.append(" AND o.quantity > COALESCE(");
            sql.append(DelischeConstants.ARRIVAL_LOT_ORDER_SUM_SUB_QUERY);
            sql.append(" ,0)");
        }
    }

    /**
     * デリスケ納品依頼サブクエリのFROM句を作成する.
     * @param searchCondition デリスケ発注検索条件
     * @param sql sql
     */
    private void generateDeliverySkuFromPhrase(final DelischeOrderSearchConditionModel searchCondition, final StringBuilder sql) {
        sql.append("   LEFT OUTER JOIN t_delivery d");
        sql.append("           ON o.id = d.order_id");
        sql.append("          AND d.deleted_at IS NULL");
        sql.append("   LEFT OUTER JOIN t_delivery_detail dd");
        sql.append("           ON d.id = dd.delivery_id");
        sql.append("          AND d.deleted_at IS NULL");
        sql.append("   LEFT OUTER JOIN t_delivery_sku ds");
        sql.append("           ON dd.id = ds.delivery_detail_id");
        sql.append("          AND ds.deleted_at IS NULL");
        sql.append("   LEFT OUTER JOIN t_order_sku os");
        sql.append("           ON d.order_id = os.order_id");
        sql.append("          AND os.deleted_at IS NULL");
        sql.append("          AND ds.color_code = os.color_code");
        sql.append("          AND ds.size = os.size");
        sql.append("   LEFT OUTER JOIN m_sizmst szm");
        sql.append("           ON szm.hscd = CONCAT(i.brand_code, i.item_code)");
        sql.append("          AND szm.szkg =  ds.size");
        sql.append("          AND szm.deleted_at IS NULL");
        sql.append("          AND szm.mntflg IN ('1', '2', '')");
    }

    /**
     * クエリにパラメータを設定する.
     * @param sql SQL文
     * @param searchCondition 検索条件
     * @param query クエリ
     */
    public void setQueryParameters(final StringBuilder sql, final DelischeOrderSearchConditionModel searchCondition,
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
    }

    /**
     * 発注残を計算する.
     * @param quantity 発注数
     * @param arrivalLotSum 入荷数
     * @return 発注残
     */
    public Integer calculateOrderRemainingLot(final Integer quantity, final Integer arrivalLotSum) {
        return NumberUtils.defaultInt(quantity) - NumberUtils.defaultInt(arrivalLotSum);
    }

    /**
     * 在庫数を計算する.
     * @param arrivalLotSum 入荷数
     * @param netSalesQuantity 純売上数
     * @return 在庫数
     */
    public Integer calculateStockLot(final Integer arrivalLotSum, final Integer netSalesQuantity) {
        return NumberUtils.defaultInt(arrivalLotSum) - NumberUtils.defaultInt(netSalesQuantity);
    }

    /**
     * 上代合計を計算する.
     * @param quantity 発注数
     * @param retailPrice 上代
     * @return 上代合計
     */
    public BigDecimal calculateRetailPrice(final Integer quantity, final BigDecimal retailPrice) {
        return NumberUtils.defaultInt(retailPrice).multiply(new BigDecimal(NumberUtils.defaultInt(quantity)));
    }

    /**
     * 下代合計を計算する.
     * @param quantity 発注数
     * @param productCost 原価
     * @return 下代合計
     */
    public BigDecimal calculateProductCost(final Integer quantity, final BigDecimal productCost) {
        return NumberUtils.defaultInt(productCost).multiply(new BigDecimal(NumberUtils.defaultInt(quantity)));
    }

    /**
     * 原価率を計算する.
     * @param retailPrice 上代
     * @param productCost 原価
     * @return 原価率
     */
    public double calculateCostRate(final BigDecimal retailPrice, final BigDecimal productCost) {
        if (retailPrice == null || retailPrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return NumberUtils.defaultInt(productCost).divide(retailPrice, ROUND_HALF_UP_THREE, BigDecimal.ROUND_DOWN)
                .multiply(new BigDecimal(HUNDRED))
                .setScale(1).doubleValue();
    }
}
