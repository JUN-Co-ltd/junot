package jp.co.jun.edi.repository.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import jp.co.jun.edi.entity.extended.ExtendedItemMisleadingRepresentationSearchResultEntity;
import jp.co.jun.edi.model.ItemMisleadingRepresentationSearchConditionModel;
import jp.co.jun.edi.repository.custom.ItemMisleadingRepresentationRepositoryCustom;
import jp.co.jun.edi.type.EntireQualityApprovalType;
import jp.co.jun.edi.type.QualityApprovalType;
import jp.co.jun.edi.type.RegistStatusType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 優良誤認検査承認一覧Repository実装クラス.
 */
public class ItemMisleadingRepresentationRepositoryImpl implements ItemMisleadingRepresentationRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    public static final String SELECT_CNT_PHRASE_SQL = "SELECT COUNT(i.id) ";

    private static final String SELECT_PHRASE_SQL =
            "  SELECT i.id id"
            + "     , i.part_no part_no"
            + "     , i.retail_price retail_price"
            + "     , i.product_name product_name"
            + "     , i.mdf_staff_code mdf_staff_code"
            + "     , i.coo_code coo_code"
            + "     , i.quality_coo_status quality_coo_status"
            + "     , i.quality_composition_status quality_composition_status"
            + "     , i.quality_harmful_status quality_harmful_status"
            + "     , sm.yugaikbn yugaikbn ";

    private static final String COMPOSITION_CODE_SUB_QUERY =
            "       ("
            + "         SELECT c.composition_code"
            + "           FROM t_composition c"
            + "          WHERE c.part_no_id = i.id"
            + "            AND c.deleted_at IS NULL"
            + "       ORDER BY c.serial_number ASC"
            + "          LIMIT 1"
            + "     ) ";

    private static final String ORDER_NUMBER_SUB_QUERY =
            "       ("
            + "         SELECT o.order_number"
            + "           FROM t_order o"
            + "          WHERE o.part_no_id = i.id"
            + "            AND o.deleted_at IS NULL"
            + "       ORDER BY o.order_number DESC"
            + "          LIMIT 1"
            + "     ) ";

    private static final String QUANTITY_SUB_QUERY =
            "       ("
            + "         SELECT o.quantity"
            + "           FROM t_order o"
            + "          WHERE o.part_no_id = i.id"
            + "            AND o.deleted_at IS NULL"
            + "       ORDER BY o.order_number DESC"
            + "          LIMIT 1"
            + "     ) ";

    private static final String MAX_APPROVAL_AT_SUB_QUERY =
            "       ("
            + "      SELECT MAX(m.approval_at)"
            + "        FROM t_misleading_representation m"
            + "       WHERE m.part_no_id = i.id"
            + "         AND m.deleted_at IS NULL"
            + "     ) ";

    private static final String SELECT_INNER_FROM_PHRASE_SQL =
            "   SELECT i1.id id"
            + "        , i1.part_no part_no"
            + "        , i1.deleted_at deleted_at "
            + "        , i1.brand_code brand_code"
            + "        , i1.item_code item_code"
            + "        , i1.year year"
            + "        , i1.season_code season_code"
            + "        , i1.regist_status regist_status"
            + "        , i1.retail_price retail_price"
            + "        , i1.product_name product_name"
            + "        , i1.mdf_staff_code mdf_staff_code"
            + "        , os.supplier_code mdf_maker_code"
            + "        , i1.coo_code coo_code"
            + "        , i1.quality_coo_status quality_coo_status"
            + "        , i1.quality_composition_status quality_composition_status"
            + "        , i1.quality_harmful_status quality_harmful_status";

    private static final String ORDER_BY_INNER_FROM_PHRASE_SQL = " ORDER BY i1.id ASC ";

    private static final String LIMIT_INNER_FROM_PHRASE_SQL = " LIMIT :limit OFFSET :offset ";

    private static final String JOIN_T_ORDER_SUPPLIER_PHRASE_SQL =
            " LEFT OUTER JOIN t_order_supplier os"
            + "            ON i1.current_product_order_supplier_id = os.id"
            + "           AND os.deleted_at IS NULL";

    private static final String JOIN_PHRASE_SQL =
            " LEFT OUTER JOIN m_sirmst sm"
            + "            ON i.mdf_maker_code = sm.sire"
            + "           AND sm.mntflg IN ('1','2',' ')"
            + "           AND sm.deleted_at IS NULL";

    /**
     * SELECT i.id id
     *   , i.part_no part_no
     *   , i.retail_price retail_price
     *   , i.product_name product_name
     *   , i.mdf_staff_code mdf_staff_code
     *   , i.coo_code coo_code
     *   , i.quality_coo_status quality_coo_status
     *   , i.quality_composition_status quality_composition_status
     *   , i.quality_harmful_status quality_harmful_status
     *   , sm.yugaikbn yugaikbn
     *   , (
     *       SELECT c.composition_code
     *         FROM t_composition c
     *        WHERE c.part_no_id = i.id
     *          AND c.deleted_at IS NULL
     *     ORDER BY c.serial_number ASC
     *        LIMIT 1
     *   ) as composition_code
     *   , (
     *       SELECT o.order_number
     *         FROM t_order o
     *        WHERE o.part_no_id = i.id
     *          AND o.deleted_at IS NULL
     *     ORDER BY o.order_number DESC
     *        LIMIT 1
     *   ) as order_number
     *   , (
     *       SELECT o.quantity
     *         FROM t_order o
     *        WHERE o.part_no_id = i.id
     *          AND o.deleted_at IS NULL
     *     ORDER BY o.order_number DESC
     *        LIMIT 1
     *   ) as quantity
     *   , (
     *     SELECT MAX(m.approval_at)
     *       FROM t_misleading_representation m
     *      WHERE m.part_no_id = i.id
     *        AND m.deleted_at IS NULL
     *   ) as approval_at
     * FROM
     *   (
     *     SELECT i1.id id
     *       , i1.part_no part_no
     *       , i1.deleted_at deleted_at
     *       , i1.brand_code brand_code
     *       , i1.item_code item_code
     *       , i1.year year
     *       , i1.season_code season_code
     *       , i1.regist_status regist_status
     *       , i1.retail_price retail_price
     *       , i1.product_name product_name
     *       , i1.mdf_staff_code mdf_staff_code
     *       , os.supplier_code mdf_maker_code
     *       , i1.coo_code coo_code
     *       , i1.quality_coo_status quality_coo_status
     *       , i1.quality_composition_status quality_composition_status
     *       , i1.quality_harmful_status quality_harmful_status
     *      FROM t_item i1
     *      LEFT OUTER JOIN t_order_supplier os
     *                   ON i1.current_product_order_supplier_id = os.id
     *                  AND os.deleted_at IS NULL
     *     WHERE i1.regist_status = 1
     *       AND i1.deleted_at IS NULL
     *       AND i1.brand_code = :brandCode
     *       AND i1.item_code = :itemCode
     *       AND i1.year = :year
     *       AND i1.season_code IN (:seasonList)
     *       AND i1.part_no = :partNo
     *       AND CASE WHEN i1.quality_composition_status = 0
     *                 AND i1.quality_coo_status = 0
     *                 AND i1.quality_harmful_status = 0
     *                THEN 0
     *                WHEN (i1.quality_composition_status = 0
     *                     OR i1.quality_composition_status = 9)
     *                 AND (i1.quality_coo_status = 0
     *                     OR i1.quality_coo_status = 9)
     *                 AND (i1.quality_harmful_status = 0
     *                     OR i1.quality_harmful_status = 9)
     *                THEN 3
     *                WHEN (i1.quality_composition_status = 5
     *                  OR i1.quality_composition_status = 9
     *                  OR i1.quality_coo_status = 9
     *                  OR i1.quality_harmful_status = 9)
     *                THEN 2
     *                ELSE 1 END
     *           IN (:qualityStatusList)
     *       AND i1.id IN (
     *                    SELECT o.part_no_id
     *                      FROM t_order o
     *                     WHERE o.deleted_at IS NULL
     *                       AND :productCorrectionDeliveryAtFrom <= o.product_correction_delivery_at
     *                       AND o.product_correction_delivery_at <= :productCorrectionDeliveryAtTo
     *       )
     *       AND i1.id IN (
     *                    SELECT m.part_no_id
     *                      FROM t_misleading_representation m
     *                     WHERE m.deleted_at IS NULL
     *                       AND :approvalAtFrom <= m.approval_at
     *                       AND m.approval_at <= :approvalAtTo
     *       )
     *     ORDER BY i1.id ASC
     *     LIMIT :limit OFFSET :offset
     *   ) i
     *   LEFT OUTER JOIN m_sirmst sm
     *     ON i.mdf_maker_code = sm.sire
     *     AND sm.mntflg IN ('1', '2', ' ')
     *     AND sm.deleted_at IS NULL
     * .
     */
    @Override
    public Page<ExtendedItemMisleadingRepresentationSearchResultEntity> findBySpec(final ItemMisleadingRepresentationSearchConditionModel searchCondition) {

        // SQL組み立て
        final StringBuilder searchSql = new StringBuilder();
        generateSelectPhrase(searchSql);
        searchSql.append(" FROM (");
        searchSql.append(SELECT_INNER_FROM_PHRASE_SQL);
        searchSql.append("   FROM t_item i1 ");
        searchSql.append(JOIN_T_ORDER_SUPPLIER_PHRASE_SQL);
        searchSql.append(generateWherePhrase(searchCondition));
        searchSql.append(ORDER_BY_INNER_FROM_PHRASE_SQL);
        searchSql.append(LIMIT_INNER_FROM_PHRASE_SQL);
        searchSql.append(" ) i ");
        searchSql.append(JOIN_PHRASE_SQL);

        // クエリパラメータ設定
        final Query serachQuery = entityManager.createNativeQuery(searchSql.toString(), ExtendedItemMisleadingRepresentationSearchResultEntity.class);
        setQueryParameters(searchSql, searchCondition, serachQuery);

        // ページング設定
        int pageIdx = searchCondition.getPage();
        int maxResults = searchCondition.getMaxResults();
        serachQuery.setParameter("offset", pageIdx * maxResults);
        serachQuery.setParameter("limit", maxResults);

        // レコード取得
        @SuppressWarnings("unchecked")
        final List<ExtendedItemMisleadingRepresentationSearchResultEntity> rslt = serachQuery.getResultList();

        final PageRequest pageRequest = PageRequest.of(searchCondition.getPage(), searchCondition.getMaxResults());
        return new PageImpl<>(rslt, pageRequest, countBySpec(searchCondition));
    }

    /**
     * SELECT句を作成する.
     * @param sql sql
     */
    private void generateSelectPhrase(final StringBuilder sql) {
        sql.append(SELECT_PHRASE_SQL);
        sql.append(" ,");
        sql.append(COMPOSITION_CODE_SUB_QUERY);
        sql.append(" as composition_code,");
        sql.append(ORDER_NUMBER_SUB_QUERY);
        sql.append(" as order_number,");
        sql.append(QUANTITY_SUB_QUERY);
        sql.append(" as quantity,");
        sql.append(MAX_APPROVAL_AT_SUB_QUERY);
        sql.append(" as approval_at ");
    }

    /**
     * WHERE句を作成する.
     * @param searchCondition 検索条件
     * @return Where句
     */
    private StringBuilder generateWherePhrase(final ItemMisleadingRepresentationSearchConditionModel searchCondition) {
        final StringBuilder sql = new StringBuilder();

        final String brandCode = searchCondition.getBrandCode();
        final String itemCode = searchCondition.getItemCode();
        final Integer year = searchCondition.getYear();
        final String partNoKind = searchCondition.getPartNoKind();
        final String partNoSerialNo = searchCondition.getPartNoSerialNo();
        final List<String> seasonList = searchCondition.getSubSeasonCodeList();
        final List<EntireQualityApprovalType> qualityStatusList = searchCondition.getQualityStatusList();
        final Date approvalAtFrom = searchCondition.getApprovalAtFrom();
        final Date approvalAtTo = searchCondition.getApprovalAtTo();
        final Date productCorrectionDeliveryAtFrom = searchCondition.getProductCorrectionDeliveryAtFrom();
        final Date productCorrectionDeliveryAtTo = searchCondition.getProductCorrectionDeliveryAtTo();

        sql.append("   WHERE i1.regist_status = " + RegistStatusType.PART.getValue());
        sql.append("     AND i1.deleted_at IS NULL");

        if (!StringUtils.isEmpty(brandCode)) {
            sql.append(" AND i1.brand_code = :brandCode");
        }
        if (!StringUtils.isEmpty(itemCode)) {
            sql.append(" AND i1.item_code = :itemCode");
        }
        if (!Objects.isNull(year)) {
            sql.append(" AND i1.year = :year");
        }
        if (!Objects.isNull(seasonList) && !seasonList.isEmpty()) {
            sql.append(" AND i1.season_code IN (:seasonList)");
        }
        if (!StringUtils.isEmpty(partNoKind) || !StringUtils.isEmpty(partNoSerialNo)) {
            sql.append(" AND i1.part_no = :partNo");
        }
        if (!Objects.isNull(qualityStatusList) && !qualityStatusList.isEmpty()) {
            sql.append(" AND CASE WHEN i1.quality_composition_status = " + QualityApprovalType.NON_TARGET.getValue());
            sql.append("           AND i1.quality_coo_status = " + QualityApprovalType.NON_TARGET.getValue());
            sql.append("           AND i1.quality_harmful_status = " + QualityApprovalType.NON_TARGET.getValue());
            sql.append("          THEN " + EntireQualityApprovalType.ENTIRE_NON_TARGET.getValue());
            sql.append("          WHEN (i1.quality_composition_status = " + QualityApprovalType.NON_TARGET.getValue());
            sql.append("               OR i1.quality_composition_status = " + QualityApprovalType.ACCEPT.getValue() + ")");
            sql.append("           AND (i1.quality_coo_status = " + QualityApprovalType.NON_TARGET.getValue());
            sql.append("               OR i1.quality_coo_status = " + QualityApprovalType.ACCEPT.getValue() + ")");
            sql.append("           AND (i1.quality_harmful_status = " + QualityApprovalType.NON_TARGET.getValue());
            sql.append("               OR i1.quality_harmful_status = " + QualityApprovalType.ACCEPT.getValue() + ")");
            sql.append("          THEN " + EntireQualityApprovalType.ENTIRE_INSPECTED.getValue());
            sql.append("          WHEN (i1.quality_composition_status = " + QualityApprovalType.PART.getValue());
            sql.append("            OR i1.quality_composition_status = " + QualityApprovalType.ACCEPT.getValue());
            sql.append("            OR i1.quality_coo_status = " + QualityApprovalType.ACCEPT.getValue());
            sql.append("            OR i1.quality_harmful_status = " + QualityApprovalType.ACCEPT.getValue() + ")");
            sql.append("          THEN " + EntireQualityApprovalType.PART_INSPECTED.getValue());
            sql.append("          ELSE " + EntireQualityApprovalType.ENTIRE_NON_INSPECTED.getValue());
            sql.append("     END IN (:qualityStatusList)");
        }

        // 生産(修正)納期
        if (!Objects.isNull(productCorrectionDeliveryAtFrom) || !Objects.isNull(productCorrectionDeliveryAtTo)) {
            sql.append(" AND i1.id IN (");
            sql.append("              SELECT o.part_no_id");
            sql.append("                FROM t_order o");
            sql.append("               WHERE o.deleted_at IS NULL");
            if (!Objects.isNull(productCorrectionDeliveryAtFrom)) {
                sql.append("             AND :productCorrectionDeliveryAtFrom <= o.product_correction_delivery_at");
            }
            if (!Objects.isNull(productCorrectionDeliveryAtTo)) {
                sql.append("             AND o.product_correction_delivery_at <= :productCorrectionDeliveryAtTo");
            }
            sql.append(" )");
        }

        // 承認日
        if (!Objects.isNull(approvalAtFrom) || !Objects.isNull(approvalAtTo)) {
            sql.append(" AND i1.id IN (");
            sql.append("              SELECT m.part_no_id");
            sql.append("                FROM t_misleading_representation m");
            sql.append("               WHERE m.deleted_at IS NULL");
            if (!Objects.isNull(approvalAtFrom)) {
                sql.append("             AND :approvalAtFrom <= m.approval_at");
            }
            if (!Objects.isNull(approvalAtTo)) {
                sql.append("             AND m.approval_at <= :approvalAtTo");
            }
            sql.append(" )");
        }

        return sql;
    }

    /**
     * クエリにパラメータを設定する.
     * ※件数取得SQLでも使用するのでここでページングのパラメータ設定をしないでください。
     * @param sql SQL文
     * @param searchCondition 検索条件
     * @param query クエリ
     */
    private void setQueryParameters(final StringBuilder sql, final ItemMisleadingRepresentationSearchConditionModel searchCondition,
            final Query query) {
        final String brandCode = searchCondition.getBrandCode();
        final String itemCode = searchCondition.getItemCode();
        final Integer year = searchCondition.getYear();
        final String partNoKind = searchCondition.getPartNoKind();
        final String partNoSerialNo = searchCondition.getPartNoSerialNo();
        final List<String> seasonList = searchCondition.getSubSeasonCodeList();
        final List<EntireQualityApprovalType> qualityStatusList = searchCondition.getQualityStatusList();
        final Date approvalAtFrom = searchCondition.getApprovalAtFrom();
        final Date approvalAtTo = searchCondition.getApprovalAtTo();
        final Date productCorrectionDeliveryAtFrom = searchCondition.getProductCorrectionDeliveryAtFrom();
        final Date productCorrectionDeliveryAtTo = searchCondition.getProductCorrectionDeliveryAtTo();

        if (!StringUtils.isEmpty(brandCode)) {
            query.setParameter("brandCode", brandCode);
        }
        if (!StringUtils.isEmpty(itemCode)) {
            query.setParameter("itemCode", itemCode);
        }
        if (!Objects.isNull(year)) {
            query.setParameter("year", year);
        }
        if (!StringUtils.isEmpty(partNoKind) || !StringUtils.isEmpty(partNoSerialNo)) {
            query.setParameter("partNo", partNoKind + partNoSerialNo);
        }
        if (!Objects.isNull(seasonList) && seasonList.size() > 0) {
            query.setParameter("seasonList", seasonList);
        }
        if (!Objects.isNull(qualityStatusList) && qualityStatusList.size() > 0) {
            query.setParameter("qualityStatusList", qualityStatusList.stream().map(EntireQualityApprovalType::getValue).collect(Collectors.toList()));
        }
        if (!Objects.isNull(approvalAtFrom)) {
            query.setParameter("approvalAtFrom", DateUtils.truncateDate(approvalAtFrom));
        }
        if (!Objects.isNull(approvalAtTo)) {
            query.setParameter("approvalAtTo", DateUtils.truncateDate(approvalAtTo));
        }
        if (!Objects.isNull(productCorrectionDeliveryAtFrom)) {
            query.setParameter("productCorrectionDeliveryAtFrom", DateUtils.truncateDate(productCorrectionDeliveryAtFrom));
        }
        if (!Objects.isNull(productCorrectionDeliveryAtTo)) {
            query.setParameter("productCorrectionDeliveryAtTo", DateUtils.truncateDate(productCorrectionDeliveryAtTo));
        }
    }

    /**
     * 検索条件に該当する件数を取得する.
     * @param searchCondition 検索条件
     * @return 件数
     */
    private int countBySpec(final ItemMisleadingRepresentationSearchConditionModel searchCondition) {
        final StringBuilder cntSql = new StringBuilder();
        cntSql.append(SELECT_CNT_PHRASE_SQL);
        cntSql.append(" FROM (");
        cntSql.append(SELECT_INNER_FROM_PHRASE_SQL);
        cntSql.append("   FROM t_item i1 ");
        cntSql.append(JOIN_T_ORDER_SUPPLIER_PHRASE_SQL);
        cntSql.append(generateWherePhrase(searchCondition));
        cntSql.append(" ) i ");

        final Query cntQuery = entityManager.createNativeQuery(cntSql.toString());
        setQueryParameters(cntSql, searchCondition, cntQuery);
        return ((Number) cntQuery.getSingleResult()).intValue();
    }
}
