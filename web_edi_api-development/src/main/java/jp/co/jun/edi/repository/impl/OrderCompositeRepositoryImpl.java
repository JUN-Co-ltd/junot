package jp.co.jun.edi.repository.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.OrderCompositeEntity;
import jp.co.jun.edi.model.OrderSearchConditionModel;
import jp.co.jun.edi.repository.custom.OrderCompositeRepositoryCustom;
import jp.co.jun.edi.type.CompleteOrderType;
import jp.co.jun.edi.type.FukukitaruMasterConfirmStatusType;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.OrderByType;
import jp.co.jun.edi.type.OrderCategoryType;
import jp.co.jun.edi.type.SqlQueryCriteriaType;
import jp.co.jun.edi.type.SqlQuerySpecificationType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 発注一覧検索Repositoryの実装クラス.
 */
@Slf4j
public class OrderCompositeRepositoryImpl implements OrderCompositeRepositoryCustom {

    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".repository";

    @Value("${" + PROPERTY_NAME_PREFIX + ".specification.vorder-specification.keyword-conditions-limit-size}")
    private int keywordConditionsLimitSize;

    /** SELECT句 件数. */
    private static final String SELECT_COUNT_PHRASE_SQL = "COUNT(o.id)";

    /** SELECT句. */
    private static final String SELECT_PHRASE_SQL = Arrays.asList(
            // 発注情報テーブルの取得
            "o.id AS id",
            "o.order_number AS order_number",
            "o.part_no_id AS part_no_id",
            "o.part_no AS part_no",
            "o.expense_item AS expense_item",
            "o.cut_auto_type AS cut_auto_type",
            "o.matl_maker_code AS matl_maker_code",
            "o.matl_part_no AS matl_part_no",
            "o.matl_product_name AS matl_product_name",
            "o.matl_delivery_at AS matl_delivery_at",
            "o.matl_meter AS matl_meter",
            "o.matl_unit_price AS matl_unit_price",
            "o.cloth_number AS cloth_number",
            "o.necessary_length_actual AS necessary_length_actual",
            "o.necessary_length_unit AS necessary_length_unit",
            "o.matl_cost AS matl_cost",
            "o.mdf_maker_code AS mdf_maker_code",
            "o.mdf_maker_factory_code AS mdf_maker_factory_code",
            "o.mdf_maker_factory_name AS mdf_maker_factory_name",
            "o.product_order_at AS product_order_at",
            "o.product_delivery_at AS product_delivery_at",
            "o.product_correction_delivery_at AS product_correction_delivery_at",
            "o.order_complete_at AS order_complete_at",
            "o.mdf_staff_code AS mdf_staff_code",
            "o.product_complete_order AS product_complete_order",
            "o.product_complete_type AS product_complete_type",
            "o.all_completion_type AS all_completion_type",
            "o.quantity AS quantity",
            "o.unit_price AS unit_price",
            "o.retail_price AS retail_price",
            "o.product_cost AS product_cost",
            "o.processing_cost AS processing_cost",
            "o.attached_cost AS attached_cost",
            "o.other_cost AS other_cost",
            "o.non_conforming_product_unit_price AS non_conforming_product_unit_price",
            "o.import_code AS import_code",
            "o.coo_code AS coo_code",
            "o.application AS application",
            "o.send_code AS send_code",
            "o.order_approve_status AS order_approve_status",
            "o.order_confirm_at AS order_confirm_at",
            "o.order_confirm_user_id AS order_confirm_user_id",
            "o.order_approve_at AS order_approve_at",
            "o.order_approve_user_id AS order_approve_user_id",
            "o.order_sheet_out AS order_sheet_out",
            "o.junpc_tanto AS junpc_tanto",
            "o.linking_status AS linking_status",
            "o.linked_at AS linked_at",

            // 品番情報テーブルの取得
            "i.product_name AS product_name",
            "i.year AS year",
            "i.sub_season_code AS sub_season_code",
            "i.brand_code AS brand_code",
            "i.brand_sort_code AS brand_sort_code",
            "i.item_code AS item_code",
            "i.dept_code AS dept_code",
            "i.preferred_delivery_date AS preferred_delivery_date",
            "i.planner_code AS planner_code",
            "i.pataner_code AS pataner_code",
            "i.misleading_representation AS misleading_representation",
            "i.quality_composition_status AS quality_composition_status",
            "i.quality_coo_status AS quality_coo_status",
            "i.quality_harmful_status AS quality_harmful_status",
            "i.regist_status AS regist_status",

            // 仕入先マスタテーブルの取得
            "sm.name AS mdf_maker_name",

            // 納品予定テーブルの取得
            "dp.id AS delivery_plan_id",

            // 納品予定明細件数の取得
            "("
                    + "SELECT"
                    + " COUNT(dpd.id)"
                    + " FROM"
                    + " t_delivery_plan_detail dpd"
                    + " WHERE"
                    + " dpd.delivery_plan_id = dp.id"
                    + " AND dpd.deleted_at IS NULL"
                    + ") AS delivery_plan_details_cnt",

            // 生産ステータス件数の取得
            "("
                    + "SELECT"
                    + " COUNT(ps.id)"
                    + " FROM"
                    + " t_production_status ps"
                    + " WHERE"
                    + " ps.order_id = o.id"
                    + " AND ps.deleted_at IS NULL"
                    + ")AS production_status_cnt",

            // フクキタル資材発注確定レコードの存在有無の取得
            "("
                    + "EXISTS ("
                    + "SELECT"
                    + "  fo.id"
                    + "  FROM"
                    + "  t_f_order fo"
                    + "  WHERE"
                    + "  fo.order_id = o.id"
                    + "  AND fo.deleted_at IS NULL"
                    + "  AND fo.confirm_status = " + FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED.getValue()
                    + ")) AS exists_material_order_confirm")
            .stream().collect(Collectors.joining(","));

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 発注情報を取得する.
     *
     * <pre>
     * {@code
     * SELECT
     *   o.id AS id
     *   , o.order_number AS order_number
     *   , o.part_no_id AS part_no_id
     *   , o.part_no AS part_no
     *   , o.expense_item AS expense_item
     *   , o.cut_auto_type AS cut_auto_type
     *   , o.matl_maker_code AS matl_maker_code
     *   , o.matl_part_no AS matl_part_no
     *   , o.matl_product_name AS matl_product_name
     *   , o.matl_delivery_at AS matl_delivery_at
     *   , o.matl_meter AS matl_meter
     *   , o.matl_unit_price AS matl_unit_price
     *   , o.cloth_number AS cloth_number
     *   , o.necessary_length_actual AS necessary_length_actual
     *   , o.necessary_length_unit AS necessary_length_unit
     *   , o.matl_cost AS matl_cost
     *   , o.mdf_maker_code AS mdf_maker_code
     *   , o.mdf_maker_factory_code AS mdf_maker_factory_code
     *   , o.mdf_maker_factory_name AS mdf_maker_factory_name
     *   , o.product_order_at AS product_order_at
     *   , o.product_delivery_at AS product_delivery_at
     *   , o.product_correction_delivery_at AS product_correction_delivery_at
     *   , o.order_complete_at AS order_complete_at
     *   , o.mdf_staff_code AS mdf_staff_code
     *   , o.product_complete_order AS product_complete_order
     *   , o.product_complete_type AS product_complete_type
     *   , o.all_completion_type AS all_completion_type
     *   , o.quantity AS quantity
     *   , o.unit_price AS unit_price
     *   , o.retail_price AS retail_price
     *   , o.product_cost AS product_cost
     *   , o.processing_cost AS processing_cost
     *   , o.attached_cost AS attached_cost
     *   , o.other_cost AS other_cost
     *   , o.non_conforming_product_unit_price AS non_conforming_product_unit_price
     *   , o.import_code AS import_code
     *   , o.coo_code AS coo_code
     *   , o.application AS application
     *   , o.send_code AS send_code
     *   , o.order_approve_status AS order_approve_status
     *   , o.order_confirm_at AS order_confirm_at
     *   , o.order_confirm_user_id AS order_confirm_user_id
     *   , o.order_approve_at AS order_approve_at
     *   , o.order_approve_user_id AS order_approve_user_id
     *   , o.order_sheet_out AS order_sheet_out
     *   , o.junpc_tanto AS junpc_tanto
     *   , o.linking_status AS linking_status
     *   , o.linked_at AS linked_at
     *   , i.product_name AS product_name
     *   , i.year AS year
     *   , i.sub_season_code AS sub_season_code
     *   , i.brand_code AS brand_code
     *   , i.brand_sort_code AS brand_sort_code
     *   , i.item_code AS item_code
     *   , i.dept_code AS dept_code
     *   , i.preferred_delivery_date AS preferred_delivery_date
     *   , i.planner_code AS planner_code
     *   , i.pataner_code AS pataner_code
     *   , i.misleading_representation AS misleading_representation
     *   , i.quality_composition_status AS quality_composition_status
     *   , i.quality_coo_status AS quality_coo_status
     *   , i.quality_harmful_status AS quality_harmful_status
     *   , i.regist_status AS regist_status
     *   , sm.name AS mdf_maker_name
     *   , dp.id AS delivery_plan_id
     *   , (
     *     SELECT
     *       COUNT(dpd.id)
     *     FROM
     *       t_delivery_plan_detail dpd
     *     WHERE
     *       dpd.delivery_plan_id = dp.id
     *       AND dpd.deleted_at IS NULL
     *   ) AS delivery_plan_details_cnt
     *   , (
     *     SELECT
     *       COUNT(ps.id)
     *     FROM
     *       t_production_status ps
     *     WHERE
     *       ps.order_id = o.id
     *       AND ps.deleted_at IS NULL
     *   ) AS production_status_cnt
     *   , (
     *     EXISTS (
     *       SELECT
     *         fo.id
     *       FROM
     *         t_f_order fo
     *       WHERE
     *         fo.order_id = o.id
     *         AND fo.deleted_at IS NULL
     *         AND fo.confirm_status = 1
     *     )
     *   ) AS exists_material_order_confirm
     * FROM
     *   t_order o
     *   INNER JOIN t_item i
     *     ON i.id = o.part_no_id
     *     AND i.deleted_at IS NULL
     *   INNER JOIN t_order_supplier os
     *     -- ↓↓↓ 権限によって結合項目を変更 ↓↓↓
     *     -- JUN権限の場合、発注先メーカーID(最新製品)で結合
     *     ON os.id = i.current_product_order_supplier_id
     *     -- メーカー権限の場合、品番IDとメーカーコードで結合
     *     ON os.part_no_id = i.id
     *     AND os.supplier_code = 'supplierCode'
     *     -- ↑↑↑ 権限によって結合項目を変更 ↑↑↑
     *     AND os.order_category_type = '1'
     *     AND os.deleted_at IS NULL
     *   LEFT JOIN m_sirmst sm
     *     ON sm.sire = o.mdf_maker_code
     *     AND sm.deleted_at IS NULL
     *     AND sm.mntflg IN ('1', '2', '')
     *   LEFT JOIN t_delivery_plan dp
     *     ON dp.order_id = o.id
     *     AND dp.deleted_at IS NULL
     * WHERE
     *   (
     *     o.deleted_at IS NULL
     *     AND o.id = :orderId
     *     AND o.part_no_id = :partNoId
     *     AND o.order_number = :orderNumber
     *     AND (
     *       -- 複数検索/部分一致
     *       i.part_no LIKE :partNo[0-9]
     *       -- 複数検索/部分一致
     *       OR i.product_name LIKE :productName[0-9]
     *       OR i.brand_code IN (:brandCodeList)
     *       OR i.item_code IN (:itemCodeList)
     *       -- 複数検索/コードは完全一致・名称は部分一致
     *       OR (o.mdf_maker_code = :mdfMakerCode[0-9] OR sm.name LIKE :mdfMakerName[0-9])
     *       OR o.order_number IN (:orderNumberList)
     *     )
     *     AND i.sub_season_code = :subSeasonCode
     *     AND i.year = :year
     *     AND o.product_correction_delivery_at >= :productCorrectionDeliveryAtFrom
     *     AND o.product_correction_delivery_at <= :productCorrectionDeliveryAtTo
     *     AND (
     *       EXISTS (
     *         SELECT
     *           cm.code1
     *         FROM
     *           m_codmst cm
     *         WHERE
     *           cm.tblid = '22'
     *           AND cm.code1 = i.mdf_staff_code
     *           -- コードは前方一致・名称は部分一致
     *           AND (cm.code1 LIKE :mdfStaffCode OR cm.item2 LIKE :mdfStaffName)
     *       )
     *       OR EXISTS (
     *         SELECT
     *           cm.code1
     *         FROM
     *           m_codmst cm
     *         WHERE
     *           cm.tblid = '22'
     *           AND cm.code1 = i.pataner_code
     *           -- コードは前方一致・名称は部分一致
     *           AND (cm.code1 LIKE :patanerCode OR cm.item2 LIKE :patanerName)
     *       )
     *       OR EXISTS (
     *         SELECT
     *           cm.code1
     *         FROM
     *           m_codmst cm
     *         WHERE
     *           cm.tblid = '22'
     *           AND cm.code1 = i.planner_code
     *           -- コードは前方一致・名称は部分一致
     *           AND (cm.code1 LIKE :plannerCode OR cm.item2 LIKE :plannerName)
     *       )
     *       OR EXISTS (
     *         SELECT
     *           u.id
     *         FROM
     *           m_user u
     *         WHERE
     *           u.id = os.supplier_staff_id
     *           -- コードは前方一致・名称は部分一致
     *           AND (u.account_name LIKE :supplierStaffCode OR u.name LIKE :supplierStaffName)
     *       )
     *     )
     *     -- メーカー権限の場合、指定する
     *     AND o.mdf_maker_code = :supplierCode
     *   )
     * ORDER BY
     *   -- パラメーターによって、DESC OR ASCを指定する
     *   o.id DESC
     * }
     * </pre>
     */
    @Override
    public Page<OrderCompositeEntity> findBySearchCondition(
            final OrderSearchConditionModel searchCondition,
            final String supplierCode,
            final Pageable pageable) {
        // SELECT COUNT句を生成
        final StringBuilder sqlCount = new StringBuilder();
        generateSelectCountPhrase(sqlCount);

        // FROM句を生成
        final StringBuilder sqlFrom = new StringBuilder();
        generateFromPhrase(sqlFrom, supplierCode);

        // WHERE句を生成
        final Map<String, Object> parameterMap = new HashMap<>();
        final StringBuilder sqlWhere = new StringBuilder();
        generateWherePhrase(sqlWhere, parameterMap, searchCondition, supplierCode);

        // FROM句とWHERE句を追加
        sqlCount.append(sqlFrom).append(sqlWhere);

        if (log.isDebugEnabled()) {
            log.debug("sqlCount:" + sqlCount.toString());
        }

        // 件数を取得
        final long count = QueryUtils.count(entityManager.createNativeQuery(sqlCount.toString()), parameterMap);

        if (count == 0) {
        	return new PageImpl<>(Collections.emptyList(), pageable, count);
        }

        // SELECT句を生成
        final StringBuilder sql = new StringBuilder();
        generateSelectPhrase(sql);

        // FROM句とWHERE句を追加
        sql.append(sqlFrom).append(sqlWhere);

        // ORDER BY句を生成
        generateOrderByPhrase(sql, searchCondition);

        if (log.isDebugEnabled()) {
            log.debug("sql:" + sql.toString());
        }

        final Query query = entityManager.createNativeQuery(sql.toString(), OrderCompositeEntity.class);

        // クエリにパラメータを設定
        QueryUtils.setQueryParameters(query, parameterMap);

        // 開始位置を設定
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        // 取得件数を設定
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        final List<OrderCompositeEntity> list = query.getResultList();

        return new PageImpl<>(list, pageable, count);
    }

    /**
     * SELECT句を作成する.
     * @param sql sql
     */
    private void generateSelectPhrase(final StringBuilder sql) {
        sql.append("SELECT ").append(SELECT_PHRASE_SQL);
    }

    /**
     * SELECT COUNT句を作成する.
     * @param sql sql
     */
    private void generateSelectCountPhrase(final StringBuilder sql) {
        sql.append("SELECT ").append(SELECT_COUNT_PHRASE_SQL);
    }

    /**
     * FROM句を作成する.
     * @param sql sql
     * @param supplierCode メーカーコード
     */
    private void generateFromPhrase(final StringBuilder sql, final String supplierCode) {
        sql.append(" FROM");
        sql.append(" t_order o");

        // 品番情報
        sql.append(" INNER JOIN t_item i");
        sql.append(" ON i.id = o.part_no_id");
        sql.append(" AND i.deleted_at IS NULL");

        // 生産メーカー
        sql.append(" INNER JOIN t_order_supplier os");

        if (StringUtils.isEmpty(supplierCode)) {
            // JUN権限の場合、発注先メーカーID(最新製品)で結合
            sql.append(" ON os.id = i.current_product_order_supplier_id");
        } else {
            // メーカー権限の場合、品番IDとメーカーコードで結合
            sql.append(" ON os.part_no_id = i.id");
            sql.append(" AND os.supplier_code = '");
            sql.append(supplierCode);
            sql.append("'");
        }

        sql.append(" AND os.order_category_type = '");
        sql.append(OrderCategoryType.PRODUCT.getValue());
        sql.append("'");
        sql.append(" AND os.deleted_at IS NULL");

        // 仕入先マスタ(生産メーカー)
        sql.append(" LEFT JOIN m_sirmst sm");
        sql.append(" ON sm.sire = o.mdf_maker_code");
        sql.append(" AND sm.deleted_at IS NULL");
        sql.append(" AND sm.mntflg IN ('1', '2', '')");

        // 納品予定
        sql.append(" LEFT JOIN t_delivery_plan dp");
        sql.append(" ON dp.order_id = o.id");
        sql.append(" AND dp.deleted_at IS NULL");
    }

    /**
     * WHERE句を作成する.
     * @param sql sql
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     * @param supplierCode メーカーコード（メーカー権限の場合、他メーカ―の情報を除外するため、メーカーコードを設定すること）
     */
    private void generateWherePhrase(
            final StringBuilder sql,
            final Map<String, Object> parameterMap,
            final OrderSearchConditionModel searchCondition,
            final String supplierCode) {
        final List<String> sqlColumns = new ArrayList<>();

        sqlColumns.add("o.deleted_at IS NULL");

        // 発注ID：完全一致
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "orderId", "o.id",
                SqlQueryCriteriaType.EQUAL, searchCondition.getId());

        // 品番ID：完全一致
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "partNoId", "o.part_no_id",
                SqlQueryCriteriaType.EQUAL, searchCondition.getPartNoId());

        // 発注No：完全一致
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "orderNumber", "o.order_number",
                SqlQueryCriteriaType.EQUAL, searchCondition.getOrderNumber());

        // キーワード検索
        keywordContains(sqlColumns, parameterMap, searchCondition);

        // シーズン：完全一致
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap, "subSeasonCode", "i.sub_season_code",
                SqlQueryCriteriaType.EQUAL, StringUtils.trimToNull(searchCondition.getSubSeasonCode()));

        // 年度：完全一致
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "year", "i.year",
                SqlQueryCriteriaType.EQUAL, searchCondition.getYear());

        // PRD_0107 JFE del start

//        if(searchCondition.getYear() == null) {
//        	// 年度指定が無い場合
//        	// 製品完納区分が6
//        	QueryUtils.addSqlWhere(sqlColumns, parameterMap, "PRODUCT_COMPLETE_ORDER",
//        			"o.PRODUCT_COMPLETE_ORDER", SqlQueryCriteriaType.EQUAL, "6");
//        	// 完納後2か月以内
//        	QueryUtils.addSqlWhere(sqlColumns, parameterMap, "ORDER_COMPLETE_AT",
//        			"o.ORDER_COMPLETE_AT", SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO,
//        			"2021/07/18");
//        }

        // PRD_0107 JFE del end


        // 納期
        productCorrectionDeliveryAtContains(sqlColumns, parameterMap, searchCondition);

        // 担当者
        staffContains(sqlColumns, parameterMap, searchCondition);

        // メーカー権限
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "supplierCode", "o.mdf_maker_code",
                SqlQueryCriteriaType.EQUAL, supplierCode);


        // PRD_0107 JFE mod start
        if (!sqlColumns.isEmpty()) {
            sql.append(" WHERE (").append(StringUtils.join(sqlColumns, " " + SqlQuerySpecificationType.AND + " "));
        }

        // PRD_0107 JFE mod end

        // PRD_0107 JFE mod start
        if(sqlColumns.size() == 1) { // WHEREはo.deleted_at IS NULLのみの場合

        	// 年度指定が無い場合
        	// 製品完納区分が6または5
        	sql.append("  AND (((o.PRODUCT_COMPLETE_ORDER IN ('" + CompleteOrderType.COMPLETE.getValue() + "', '"
        			+ CompleteOrderType.AUTO_COMPLETE.getValue() + "'))");
        	// 完納後3か月以内
        	sql.append(" AND o.ORDER_COMPLETE_AT >= '" + new DateTime().minusMonths(3).toString() + "')");
        	// 製品完納区分が0
        	sql.append(" OR o.PRODUCT_COMPLETE_ORDER = '" + CompleteOrderType.INCOMPLETE.getValue() + "')");

        }

        sql.append(")");

        // PRD_0107 JFE mod end
    }

    /**
     * キーワード選択.
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     */
    private void keywordContains(final List<String> sqlColumns, final Map<String, Object> parameterMap,
            final OrderSearchConditionModel searchCondition) {
        final List<String> sqlSubColumns = new ArrayList<>();

        // 品番
        partNoContains(sqlSubColumns, parameterMap,
                QueryUtils.getSplitConditions(searchCondition.getPartNo(), keywordConditionsLimitSize));

        // 品名
        productNameContains(sqlSubColumns, parameterMap,
                QueryUtils.getSplitConditions(searchCondition.getProductName(), keywordConditionsLimitSize));

        // ブランドコード
        brandCodeContains(sqlSubColumns, parameterMap,
                QueryUtils.getSplitConditions(searchCondition.getBrandCode(), keywordConditionsLimitSize));

        // アイテムコード
        itemCodeContains(sqlSubColumns, parameterMap,
                QueryUtils.getSplitConditions(searchCondition.getItemCode(), keywordConditionsLimitSize));

        // メーカー
        makerContains(sqlSubColumns, parameterMap,
                QueryUtils.getSplitConditions(searchCondition.getMaker(), keywordConditionsLimitSize));

        // 発注No
        orderNumberContains(sqlSubColumns, parameterMap,
                QueryUtils.getSplitConditionsOrderNumber(searchCondition.getOrderNumberText(), keywordConditionsLimitSize));

        if (!sqlSubColumns.isEmpty()) {
            sqlColumns.add(QueryUtils.toOrJoin(sqlSubColumns));
        }
    }

    /**
     * 品番で絞り込む(複数検索/部分一致).
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param values 値のリスト
     */
    private void partNoContains(final List<String> sqlColumns, final Map<String, Object> parameterMap,
            final List<String> values) {
        QueryUtils.addSqlWhereStringList(sqlColumns, parameterMap, "partNo", "i.part_no",
                SqlQueryCriteriaType.LIKE_PARTIAL, values);
    }

    /**
     * 品名で絞り込む(複数検索/部分一致).
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param values 値のリスト
     */
    private void productNameContains(final List<String> sqlColumns, final Map<String, Object> parameterMap,
            final List<String> values) {
        QueryUtils.addSqlWhereStringList(sqlColumns, parameterMap, "productName", "i.product_name",
                SqlQueryCriteriaType.LIKE_PARTIAL, values);
    }

    /**
     * ブランドコードで絞り込む(複数検索/完全一致).
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param values 値のリスト
     */
    private void brandCodeContains(final List<String> sqlColumns, final Map<String, Object> parameterMap,
            final List<String> values) {
        QueryUtils.addSqlWhereIn(sqlColumns, parameterMap, "brandCodeList", "i.brand_code", values);
    }

    /**
     * アイテムコードで絞り込む(複数検索/完全一致).
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param values 値のリスト
     */
    private void itemCodeContains(final List<String> sqlColumns, final Map<String, Object> parameterMap,
            final List<String> values) {
        QueryUtils.addSqlWhereIn(sqlColumns, parameterMap, "itemCodeList", "i.item_code", values);
    }

    /**
     * メーカーで絞り込む(複数検索/コードは完全一致・名称は部分一致).
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param values 値のリスト
     */
    private void makerContains(final List<String> sqlColumns, final Map<String, Object> parameterMap,
            final List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            // 値がない場合、処理を終了する
            return;
        }

        final List<String> sqlSubColumns = new ArrayList<>();

        int i = 0;

        for (final String value : values) {
            QueryUtils.addSqlWhereString(sqlSubColumns, parameterMap, "mdfMakerCode" + i, "o.mdf_maker_code", SqlQueryCriteriaType.EQUAL, value);
            QueryUtils.addSqlWhereString(sqlSubColumns, parameterMap, "mdfMakerName" + i, "sm.name", SqlQueryCriteriaType.LIKE_PARTIAL, value);

            i++;
        }

        if (!sqlSubColumns.isEmpty()) {
            sqlColumns.add(QueryUtils.toOrJoin(sqlSubColumns));
        }
    }

    /**
     * 発注Noで絞り込む(複数検索/完全一致).
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param values 値のリスト
     */
    private void orderNumberContains(final List<String> sqlColumns, final Map<String, Object> parameterMap,
            final List<BigInteger> values) {
        QueryUtils.addSqlWhereIn(sqlColumns, parameterMap, "orderNumberList", "o.order_number", values);
    }

    /**
     * 担当者検索.
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     */
    private void staffContains(final List<String> sqlColumns, final Map<String, Object> parameterMap,
            final OrderSearchConditionModel searchCondition) {
        final List<String> sqlSubColumns = new ArrayList<>();

        // 製造担当
        addSqlWhereStaffSubQuery(sqlSubColumns, parameterMap, "mdfStaff", "i.mdf_staff_code", searchCondition.getMdfStaffName());

        // パタンナー担当
        addSqlWhereStaffSubQuery(sqlSubColumns, parameterMap, "pataner", "i.pataner_code", searchCondition.getPatanerName());

        // 企画担当
        addSqlWhereStaffSubQuery(sqlSubColumns, parameterMap, "planner", "i.planner_code", searchCondition.getPlannerName());

        // 生産メーカー担当
        addSqlWhereSupplierStaffSubQuery(sqlSubColumns, parameterMap, "supplierStaff", "os.supplier_staff_id", searchCondition.getMdfMakerStaffName());

        if (!sqlSubColumns.isEmpty()) {
            sqlColumns.add(QueryUtils.toOrJoin(sqlSubColumns));
        }
    }

    /**
     * 担当者検索のサブクエリ.
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param value 値
     */
    private void addSqlWhereStaffSubQuery(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final String value) {
        if (StringUtils.isEmpty(value)) {
            // 値がない場合、処理を終了する
            return;
        }

        final List<String> sqlSubColumns = new ArrayList<>();

        QueryUtils.addSqlWhereString(sqlSubColumns, parameterMap, key + "Code", "cm.code1",
                SqlQueryCriteriaType.LIKE_FORWARD, value);
        QueryUtils.addSqlWhereString(sqlSubColumns, parameterMap, key + "Name", "cm.item2",
                SqlQueryCriteriaType.LIKE_PARTIAL, value);

        sqlColumns.add(new StringBuilder()
                .append("EXISTS (")
                .append("SELECT cm.code1")
                .append(" FROM m_codmst cm")
                .append(" WHERE cm.tblid = '")
                .append(MCodmstTblIdType.STAFF.getValue())
                .append("'")
                .append(" ")
                .append(SqlQuerySpecificationType.AND)
                .append(" cm.code1 = ")
                .append(columnName)
                .append(" ")
                .append(SqlQuerySpecificationType.AND)
                .append(" ")
                .append(QueryUtils.toOrJoin(sqlSubColumns))
                .append(")").toString());
    }

    /**
    * メーカー担当者検索のサブクエリ.
    *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param value 値
    */
    private void addSqlWhereSupplierStaffSubQuery(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final String value) {
        if (StringUtils.isEmpty(value)) {
            // 値がない場合、処理を終了する
            return;
        }

        final List<String> sqlSubColumns = new ArrayList<>();

        // 生産メーカー担当
        QueryUtils.addSqlWhereString(sqlSubColumns, parameterMap, key + "Code", "u.account_name",
                SqlQueryCriteriaType.LIKE_FORWARD, value);
        QueryUtils.addSqlWhereString(sqlSubColumns, parameterMap, key + "Name", "u.name",
                SqlQueryCriteriaType.LIKE_PARTIAL, value);

        sqlColumns.add(new StringBuilder()
                .append("EXISTS (")
                .append("SELECT u.id")
                .append(" FROM m_user u")
                .append(" WHERE")
                .append(" u.id = ")
                .append(columnName)
                .append(" ")
                .append(SqlQuerySpecificationType.AND)
                .append(" ")
                .append(QueryUtils.toOrJoin(sqlSubColumns))
                .append(")").toString());
    }

    /**
     * 製品修正納期で発注情報ビューの絞り込みを行う.
     * 開始年が入力されていない場合、最古の製品修正納期から検索.
     * 終了年が入力されていない場合、最新の製品修正納期まで検索.
     * 開始年が入力されているが開始月度が入力されていない場合、開始月度は1月度で検索.
     * 終了年が入力されているが終了月度が入力されていない場合、終了月度は12月度で検索.
     * ※月度が入力されても年がなければ範囲指定しない.
     * ※月度の例：12月度：11/21～12/20、1月度：12/21～1/20.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     */
    public void productCorrectionDeliveryAtContains(final List<String> sqlColumns, final Map<String, Object> parameterMap,
            final OrderSearchConditionModel searchCondition) {
        QueryUtils.addSqlWhereFromTo(sqlColumns, parameterMap,
                "productCorrectionDeliveryAt",
                "o.product_correction_delivery_at",
                // 開始年月日を取得
                DateUtils.toFromDate(
                        searchCondition.getProductDeliveryAtYearFrom(),
                        searchCondition.getProductDeliveryAtMonthlyFrom()),
                // 終了年月日を取得
                DateUtils.toToDate(
                        searchCondition.getProductDeliveryAtYearTo(),
                        searchCondition.getProductDeliveryAtMonthlyTo()));
    }

    /**
     * ORDER BY句を作成する.
     * @param sql sql
     * @param searchCondition 検索条件
     */
    private void generateOrderByPhrase(final StringBuilder sql, final OrderSearchConditionModel searchCondition) {
        sql.append(" ORDER BY");

        if (OrderByType.DESC == OrderByType.convertToType(searchCondition.getIdOrderBy())) {
            sql.append(" o.id DESC");
        } else {
            sql.append(" o.id ASC");
        }
    }
}
