package jp.co.jun.edi.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.MakerReturnProductCompositeEntity;
import jp.co.jun.edi.model.MakerReturnProductSearchConditionModel;
import jp.co.jun.edi.repository.custom.MakerReturnProductCompositeRepositoryCustom;
import jp.co.jun.edi.type.SqlQueryCriteriaType;
import jp.co.jun.edi.type.SqlQuerySpecificationType;
import jp.co.jun.edi.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * メーカー返品商品情報Repositoryの実装クラス.
 */
@Slf4j
public class MakerReturnProductCompositeRepositoryImpl implements MakerReturnProductCompositeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /** SELECT句. */
    private static final String SELECT_PHRASE = "SELECT "
            + "os.color_code"
            + ", os.size"
            + ", o.id as order_id"
            + ", o.part_no_id"
            + ", o.part_no"
            + ", o.order_number"
            + ", o.product_order_at"
            + ", o.retail_price"
            + ", CASE WHEN o.non_conforming_product_unit_price > 0"
            + "       THEN o.non_conforming_product_unit_price"
            + "       ELSE o.unit_price"
            + "  END unit_price"
            + ", i.product_name"
            + ", i.brand_code"
            + ", i.item_code"
            + ", i.other_cost"
            + ", s.stock_lot "
            + ", CAST(ms.jun AS SIGNED)";

    /** ORDER BY句. */
    private static final String ORDER_BY_PHRASE =
            " ORDER BY "
                    + " part_no ASC"
                    + ", color_code ASC"
                    + ", jun ASC"
                    + ", order_number DESC ";

    /**
     * メーカー返品商品情報を取得する.
     * SELECT
     *   os.color_code
     *   , os.size
     *   , o.id as order_id
     *   , o.part_no_id
     *   , o.part_no
     *   , o.order_number
     *   , o.product_order_at
     *   , o.retail_price
     *   , o.unit_price
     *   , i.product_name
     *   , i.brand_code
     *   , i.item_code
     *   , i.other_cost
     *   , s.stock_lot
     *   , CAST(ms.jun AS SIGNED)
     * FROM
     *   t_order_sku os
     *   INNER JOIN t_shop_stock s
     *     ON s.part_no = os.part_no
     *     AND s.color_code = os.color_code
     *     AND s.size = os.size
     *     AND s.deleted_at IS NULL
     *     AND s.shop_code = :shpcd
     *   INNER JOIN t_order o
     *     ON o.id = os.order_id
     *     AND o.deleted_at IS NULL
     *     AND o.part_no = :partNo
     *     AND o.part_no = :partNoOfProductCode
     *     AND o.mdf_maker_code = :supplierCode
     *     AND o.retail_price >= :retailPriceFrom
     *     AND o.retail_price <= :retailPriceTo
     *   INNER JOIN t_item i
     *     ON i.id = o.part_no_id
     *     AND i.deleted_at IS NULL
     *     AND i.brand_code IN (:brandCodes)
     *     AND i.item_code IN (:itemCodes)
     *     AND i.product_name LIKE :productName
     *     AND i.sub_season_code IN (:seasons)
     *   -- PRD_0115 入荷済み条件追加↓
     *   INNER JOIN
     *        (
     *            select
     *                dh.order_id
     *            from
     *                t_delivery dh
     *            join
     *                t_delivery_detail dd
     *                    on dh.id = dd.delivery_id
     *                    AND arrival_flg = '1'
     *                    AND dd.deleted_at IS NULL
     *            group by
     *                dh.order_id
     *   )  d
     *    ON os.order_id = d.order_id
     *   -- PRD_0115 入荷済み条件追加↑
     *   LEFT OUTER JOIN m_sizmst ms
     *     ON ms.hscd = LEFT (os.part_no, 3)
     *     AND ms.szkg = os.size
     *     AND ms.mntflg IN ('1', '2', '')
     *     AND ms.deleted_at IS NULL
     * WHERE
     *   os.deleted_at IS NULL
     *   AND  os.net_purchase_lot >= 1  -- PRD_0115 純仕入数≧1
     *   AND os.color_code IN (:colorCodes)
     *   AND os.color_code = :colorCodeOfProductCode
     *   AND os.size IN (:sizeList)
     *   AND os.size = :sizeOfProductCode
     *   AND os.order_id IN (
     *     SELECT
     *       MAX(os.order_id) as order_id
     *     FROM
     *       t_order_sku os
     *     WHERE
     *       os.deleted_at IS NULL
     *       AND  os.net_purchase_lot >= 1  -- PRD_0115 純仕入数≧1
     *       AND os.color_code IN (:colorCodes)
     *       AND os.color_code = :colorCodeOfProductCode
     *       AND os.size IN (:sizeList)
     *       AND os.size = :sizeOfProductCode
     *       AND os.order_id IN (
     *         SELECT
     *           o.id
     *         FROM
     *           t_order o
     *         WHERE
     *           o.deleted_at IS NULL
     *           AND o.part_no = :partNo
     *           AND o.part_no = :partNoOfProductCode
     *           AND o.mdf_maker_code = :supplierCode
     *           AND o.retail_price >= :retailPriceFrom
     *           AND o.retail_price <= :retailPriceTo
     *       )
     *       AND os.part_no IN (
     *         SELECT
     *           i.part_no
     *         FROM
     *           t_item i
     *         WHERE
     *           i.deleted_at IS NULL
     *           AND i.brand_code IN (:brandCodes)
     *           AND i.item_code IN (:itemCodes)
     *           AND i.product_name LIKE :productName
     *           AND i.sub_season_code IN (:seasons)
     *       )
     *     GROUP BY
     *       os.part_no
     *   )
     * ORDER BY
     *   part_no ASC
     *   , color_code ASC
     *   , jun ASC
     *   , order_number DESC
     */
    @Override
    public Page<MakerReturnProductCompositeEntity> findBySearchCondition(
            final MakerReturnProductSearchConditionModel searchCondition,
            final Pageable pageable) {

        final Map<String, Object> parameterMap = new HashMap<>();
        final String tShopStockSearchConditions = generateTShopStockearchConditions(parameterMap, searchCondition);
        final String tOrderSearchConditions = generateTOrderSearchConditions(parameterMap, searchCondition);
        final String tItemSearchConditions = generateTItemSearchConditions(parameterMap, searchCondition);
        final StringBuilder tOrderSkuWherePhrase = generateTOrderSkuWherePhrase(parameterMap, searchCondition);

        // 件数
        final StringBuilder cntSql = generateSql(
                "SELECT COUNT(o.id)",
                tShopStockSearchConditions,
                tOrderSearchConditions,
                tItemSearchConditions,
                tOrderSkuWherePhrase,
                searchCondition.isLatestOrderOnly());

        if (log.isDebugEnabled()) {
            log.debug("sqlCount:" + cntSql.toString());
        }

        final Query cntQuery = entityManager.createNativeQuery(cntSql.toString());
        final long count =  QueryUtils.count(cntQuery, parameterMap);
        if (count == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, count);
        }

        // レコード取得
        final StringBuilder sql = generateSql(
                SELECT_PHRASE,
                tShopStockSearchConditions,
                tOrderSearchConditions,
                tItemSearchConditions,
                tOrderSkuWherePhrase,
                searchCondition.isLatestOrderOnly())
                .append(ORDER_BY_PHRASE);

        if (log.isDebugEnabled()) {
            log.debug("sql:" + sql.toString());
        }

        final Query query = entityManager.createNativeQuery(sql.toString(), MakerReturnProductCompositeEntity.class);

        // クエリにパラメータを設定
        QueryUtils.setQueryParameters(query, parameterMap);

        // 開始位置を設定
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        // 取得件数を設定
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        final List<MakerReturnProductCompositeEntity> result = query.getResultList();

        return new PageImpl<>(result, pageable, count);
    }

    /**
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     * @return t_shop_stockの検索条件
     */
    private String generateTShopStockearchConditions(
            final Map<String, Object> parameterMap,
            final MakerReturnProductSearchConditionModel searchCondition) {
        final List<String> sqlColumns = new ArrayList<>();

        sqlColumns.add(" s.color_code = os.color_code ");
        sqlColumns.add(" s.size = os.size ");
        sqlColumns.add(" s.deleted_at IS NULL ");
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap, "shpcd", "s.shop_code",
                SqlQueryCriteriaType.EQUAL, searchCondition.getShpcd());

        return StringUtils.join(sqlColumns, " " + SqlQuerySpecificationType.AND + " ");
    }

    /**
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     * @return t_orderの検索条件
     */
    private String generateTOrderSearchConditions(
            final Map<String, Object> parameterMap,
            final MakerReturnProductSearchConditionModel searchCondition) {
        final List<String> sqlColumns = new ArrayList<>();

        sqlColumns.add(" o.deleted_at IS NULL ");
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap, "partNo", "o.part_no",
                SqlQueryCriteriaType.EQUAL, searchCondition.getPartNo());
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap, "partNoOfProductCode", "o.part_no",
                SqlQueryCriteriaType.EQUAL, searchCondition.getPartNoOfProductCode());
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap, "supplierCode", "o.mdf_maker_code",
                SqlQueryCriteriaType.EQUAL, searchCondition.getSupplierCode());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "retailPriceFrom", "o.retail_price",
                SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO, searchCondition.getRetailPriceFrom());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "retailPriceTo", "o.retail_price",
                SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO, searchCondition.getRetailPriceTo());

        return StringUtils.join(sqlColumns, " " + SqlQuerySpecificationType.AND + " ");
    }

    /**
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     * @return t_itemの検索条件
     */
    private String generateTItemSearchConditions(
            final Map<String, Object> parameterMap,
            final MakerReturnProductSearchConditionModel searchCondition) {
        final List<String> sqlColumns = new ArrayList<>();

        sqlColumns.add(" i.deleted_at IS NULL ");
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "brandCodes", "i.brand_code",
                searchCondition.getBrandCodes());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "itemCodes", "i.item_code",
                searchCondition.getItemCodes());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "subSeasonCodes", "i.sub_season_code",
                searchCondition.getSubSeasonCodes());
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap, "productName", "i.product_name",
                SqlQueryCriteriaType.LIKE_PARTIAL, searchCondition.getProductName());

        return StringUtils.join(sqlColumns, " " + SqlQuerySpecificationType.AND + " ");
    }

    /**
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     * @return t_order_skuのWHERE句
     */
    private StringBuilder generateTOrderSkuWherePhrase(
            final Map<String, Object> parameterMap,
            final MakerReturnProductSearchConditionModel searchCondition) {
        final List<String> sqlColumns = new ArrayList<>();

        sqlColumns.add(" os.deleted_at IS NULL ");
        // PRD_0115 add 「発注SKU．純仕入数≧1」追加 START
        sqlColumns.add(" os.net_purchase_lot >= 1 ");
        // PRD_0115 add 「発注SKU．純仕入数≧1」追加   END
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "colorCodes", "os.color_code",
                searchCondition.getColorCodes());
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap, "colorCodeOfProductCode", "os.color_code",
                SqlQueryCriteriaType.EQUAL, searchCondition.getColorCodeOfProductCode());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "sizeList", "os.size",
                searchCondition.getSizeList());
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap, "sizeOfProductCode", "os.size",
                SqlQueryCriteriaType.EQUAL, searchCondition.getSizeOfProductCode());
        if (searchCondition.isLatestOrderOnly()) {
            sqlColumns.add(" os.order_id " + SqlQueryCriteriaType.IN + " ( ");
        }

        return new StringBuilder().append(" WHERE ").append(StringUtils.join(sqlColumns, " " + SqlQuerySpecificationType.AND + " "));
    }

    /**
     * @param selectPhrase SELECT句
     * @param tShopStockSearchConditions t_shop_stockの検索条件
     * @param tOrderSearchConditions t_orderの検索条件
     * @param tItemSearchConditions t_itemの検索条件
     * @param tOrderSkuWherePhrase t_order_skuのWHERE句
     * @param isLatestOrderOnly 最新発注分のみ表示
     * @return SQL文
     */
    private StringBuilder generateSql(
            final String selectPhrase,
            final String tShopStockSearchConditions,
            final String tOrderSearchConditions,
            final String tItemSearchConditions,
            final StringBuilder tOrderSkuWherePhrase,
            final boolean isLatestOrderOnly) {
        final StringBuilder sb = new StringBuilder()
                .append(selectPhrase)
                .append(" FROM t_order_sku os")
                .append("   INNER JOIN t_shop_stock s")
                .append("     ON s.part_no = os.part_no")
                .append("     AND ")
                .append(tShopStockSearchConditions)
                .append("   INNER JOIN t_order o")
                .append("     ON o.id = os.order_id")
                .append("     AND ")
                .append(tOrderSearchConditions)
                .append("   INNER JOIN t_item i")
                .append("     ON i.id = o.part_no_id")
                .append("     AND ")
                .append(tItemSearchConditions)
                // PRD_0115 add 「入荷フラグ＝1」追加 START
                /* 納品情報から仕入済みデータ取得 */
                .append(" INNER JOIN (")
                .append("     Select dh.order_id From t_delivery dh")
                .append("     Join t_delivery_detail dd On dh.id = dd.delivery_id")
                .append("     AND arrival_flg = '1' AND dd.deleted_at IS NULL ")
                .append("     Group by dh.order_id)  d ON os.order_id = d.order_id")
                // PRD_0115 add 「入荷フラグ＝1」追加   END
                .append("   LEFT OUTER JOIN m_sizmst ms")
                .append("     ON ms.hscd = LEFT (os.part_no, 3)")
                .append("     AND ms.szkg = os.size")
                .append("     AND ms.mntflg IN ('1', '2', '') ")
                .append("     AND ms.deleted_at IS NULL")
                .append(tOrderSkuWherePhrase);
        if (!isLatestOrderOnly) {
            return sb;
        }

        return sb.append("  SELECT MAX(os.order_id) as order_id")
                .append("     FROM t_order_sku os ")
                .append(tOrderSkuWherePhrase)
                .append("         SELECT o.id")
                .append("         FROM t_order o WHERE ")
                .append(tOrderSearchConditions)
                .append("     ) AND os.part_no IN (")
                .append("         SELECT i.part_no")
                .append("         FROM t_item i")
                .append("         WHERE ")
                .append(tItemSearchConditions)
                .append("     ) GROUP BY os.part_no)");
    }
}
