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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.component.ShipmentComponent;
import jp.co.jun.edi.entity.PurchaseCompositeEntity;
import jp.co.jun.edi.model.PurchaseSearchConditionModel;
import jp.co.jun.edi.repository.custom.PurchaseCompositeRepositoryCustom;
import jp.co.jun.edi.type.ApprovalType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.OnOffType;
import jp.co.jun.edi.type.SqlQueryCriteriaType;
import jp.co.jun.edi.type.SqlQuerySpecificationType;
import jp.co.jun.edi.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 仕入一覧情報Repositoryの実装クラス.
 */
@Slf4j
public class PurchaseCompositeRepositoryImpl implements PurchaseCompositeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ShipmentComponent shipmentComponent;

    /**
     * 仕入一覧情報を取得する.
     *
     * SELECT
     *   dd.id AS id
     *   , dd.delivery_id AS delivery_id
     *   , d.order_number AS order_number
     *   , d.delivery_count AS delivery_count
     *   , dd.delivery_number AS delivery_number
     *   , dd.division_code AS division_code
     *   , dd.carry_type AS carry_type
     *   , dd.arrival_flg AS arrival_flg
     *   , dd.correction_at AS correction_at
     *   , o.mdf_maker_code AS mdf_maker_code
     *   , sm.name as mdf_maker_name
     *   , d.part_no AS part_no
     *   , i.product_name AS product_name
     *   , p.lg_send_type AS lg_send_type
     *   , SUM(ds.delivery_lot) AS delivery_lot
     *   , COALESCE(SUM(p.arrival_count), 0) AS arrival_count_sum
     *   , COALESCE(SUM(p.fix_arrival_count), 0) AS fix_arrival_count_sum
     *   , (
     *     SELECT
     *       COUNT(p.id)
     *     FROM
     *       t_purchase p
     *     WHERE
     *       p.division_code = dd.division_code
     *       AND p.purchase_count = dd.delivery_count
     *       AND p.order_number = d.order_number
     *   ) AS purchase_registered_count
     *   , (
     *     SELECT
     *       COUNT(p.id)
     *     FROM
     *       t_purchase p
     *     WHERE
     *       p.division_code = dd.division_code
     *       AND p.purchase_count = dd.delivery_count
     *       AND p.order_number = d.order_number
     *       AND p.lg_send_type != 0
     *   ) AS purchase_confirmed_count
     * FROM
     *   t_delivery_detail dd
     *   INNER JOIN t_delivery d
     *     ON dd.delivery_id = d.id
     *     AND d.delivery_approve_status = 1
     *     AND d.deleted_at IS NULL
     *   INNER JOIN t_delivery_sku ds
     *     ON dd.id = ds.delivery_detail_id
     *     AND ds.deleted_at IS NULL
     *   INNER JOIN t_item i
     *     ON d.part_no_id = i.id
     *     AND i.deleted_at IS NULL
     *   INNER JOIN t_order o
     *     ON d.order_id = o.id
     *     AND o.deleted_at IS NULL
     *     AND o.mdf_maker_code = :mdfMakerCode
     *   INNER JOIN t_purchase p -- LG送信区分パラメータない場合はLEFT OUTET JOIN
     *     ON dd.division_code = p.division_code
     *     AND dd.delivery_count = p.purchase_count
     *     AND dd.delivery_id = p.delivery_id
     *     AND ds.color_code = p.color_code
     *     AND ds.size = p.size
     *     AND p.deleted_at IS NULL
     *     AND CASE :lgSendFlg
     *       WHEN true THEN p.lg_send_type = 1
     *       ELSE p.lg_send_type = 0
     *       END
     *   LEFT OUTER JOIN m_sirmst sm
     *     ON o.mdf_maker_code = sm.sire
     *     AND sm.deleted_at IS NULL
     *     AND sm.mntflg IN ('1', '2', '')
     * WHERE
     *   dd.deleted_at IS NULL
     *   AND COALESCE(dd.arrival_place, LEFT(dd.logistics_code,1) = :logisticsCode
     *   AND :correctionAtFrom <= dd.correction_at
     *   AND dd.correction_at <= :correctionAtTo
     *   AND :deliveryNoFrom <= CAST(dd.delivery_number AS UNSIGNED)
     *   AND CAST(dd.delivery_number AS UNSIGNED) <= :deliveryNumberTo
     *   AND dd.arriva_flg = :arrivalFlg
     *   AND :arrivalAtFrom <= dd.arrival_at
     *   AND dd.arrival_at <= :arrivalAtTo
     * GROUP BY
     *   dd.delivery_id
     *   , dd.division_code
     * ORDER BY
     *   order_number
     *   , delivery_count
     *   , division_code
     */
    @Override
    public Page<PurchaseCompositeEntity> findBySearchCondition(
            final PurchaseSearchConditionModel searchCondition,
            final Pageable pageable) {

        final StringBuilder sqlWhere = new StringBuilder();

        final Map<String, Object> parameterMap = new HashMap<>();

        generateWherePhrase(sqlWhere, parameterMap, searchCondition);

        // 件数
        final long count = countAllRecord(sqlWhere, parameterMap, searchCondition);
        if (count == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, count);
        }

        final StringBuilder sql = new StringBuilder();
        generateSelectPhrase(sql);

        final StringBuilder sqlFrom = new StringBuilder();
        generateFromPhrase(sqlFrom, searchCondition, parameterMap, false);

        sql.append(sqlFrom).append(sqlWhere);

        generateGroupByPhrase(sql);
        generateOrderByPhrase(sql);

        if (log.isDebugEnabled()) {
            log.debug("sql:" + sql.toString());
        }

        final Query query = entityManager.createNativeQuery(sql.toString(), PurchaseCompositeEntity.class);

        // クエリにパラメータを設定
        QueryUtils.setQueryParameters(query, parameterMap);

        // 開始位置を設定
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        // 取得件数を設定
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        final List<PurchaseCompositeEntity> result = query.getResultList();

        return new PageImpl<>(result, pageable, count);
    }

    /**
     * @param sqlWhere WHERE句
     * @param parameterMap パラメータ
     * @param searchCondition 検索条件
     * @return レコード件数
     */
    private long countAllRecord(final StringBuilder sqlWhere, final Map<String, Object> parameterMap, final PurchaseSearchConditionModel searchCondition) {
        final StringBuilder sqlCount = new StringBuilder();
        final StringBuilder sqlFrom = new StringBuilder();

        sqlCount.append(" SELECT COUNT(rslt.cnt)");
        sqlCount.append(" FROM (SELECT COUNT(dd.id) AS cnt ");

        generateFromPhrase(sqlFrom, searchCondition, parameterMap, true);
        sqlCount.append(sqlFrom).append(sqlWhere);

        generateGroupByPhrase(sqlCount);

        sqlCount.append(" ) AS rslt ");

        if (log.isDebugEnabled()) {
            log.debug("sqlCount:" + sqlCount.toString());
        }

        final Query query = entityManager.createNativeQuery(sqlCount.toString());
        return QueryUtils.count(query, parameterMap);
    }

    /**
     * SELECT句を生成.
     * @param sql sql
     */
    private void generateSelectPhrase(final StringBuilder sql) {
        final List<String> sqlColumns = new ArrayList<>();

        sqlColumns.add("dd.id AS id");
        sqlColumns.add("dd.delivery_id AS delivery_id");
        sqlColumns.add("d.order_number AS order_number");
        sqlColumns.add("d.delivery_count AS delivery_count");
        sqlColumns.add("dd.delivery_number AS delivery_number");
        sqlColumns.add("dd.division_code AS division_code");
        sqlColumns.add("dd.carry_type AS carry_type");
        sqlColumns.add("dd.arrival_flg AS arrival_flg");
        sqlColumns.add("dd.correction_at AS correction_at");
        sqlColumns.add("o.mdf_maker_code AS mdf_maker_code");
        sqlColumns.add("sm.name AS mdf_maker_name");
        sqlColumns.add("d.part_no AS part_no");
        sqlColumns.add("i.product_name AS product_name");
        sqlColumns.add("p.lg_send_type AS lg_send_type");
        sqlColumns.add("SUM(ds.delivery_lot) AS delivery_lot");
        sqlColumns.add("COALESCE(SUM(p.arrival_count), 0) AS arrival_count_sum");
        sqlColumns.add("COALESCE(SUM(p.fix_arrival_count), 0) AS fix_arrival_count_sum");
        // PRD_0008 mod SIT start
        //sqlColumns.add("(SELECT COUNT(p.id) "
        //        + "FROM t_purchase p "
        //        + "WHERE p.division_code = dd.division_code "
        //        + "AND p.purchase_count = dd.delivery_count "
        //        + "AND p.order_number = d.order_number) "
        //        + "AS purchase_registered_count");
        //sqlColumns.add("(SELECT COUNT(p.id) "
        //        + "FROM t_purchase p "
        //        + "WHERE p.division_code = dd.division_code "
        //        + "AND p.purchase_count = dd.delivery_count "
        //        + "AND p.order_number = d.order_number "
        //         + "AND p.lg_send_type != 0) "
        //        + "AS purchase_confirmed_count");
        sqlColumns.add("COALESCE(SUM(prc.purchase_registered_count), 0) AS purchase_registered_count");
        sqlColumns.add("COALESCE(SUM(pcc.purchase_confirmed_count), 0) AS purchase_confirmed_count");
        // PRD_0008 mod SIT end

        sql.append("SELECT ").append(StringUtils.join(sqlColumns, ", "));
    }

    /**
     * FROM句を生成.
     * @param sql sql
     * @param searchCondition 検索条件
     * @param parameterMap パラメーターマップ
     * @param isCount 件数取得用
     */
    private void generateFromPhrase(final StringBuilder sql,
            final PurchaseSearchConditionModel searchCondition,
            final Map<String, Object> parameterMap,
            final boolean isCount) {
        sql.append(" FROM t_delivery_detail dd");

        sql.append(" INNER JOIN t_delivery d");
        sql.append(" ON dd.delivery_id = d.id");
        sql.append(" AND d.delivery_approve_status = " + ApprovalType.APPROVAL.getValue());
        sql.append(" AND d.deleted_at IS NULL");

        sql.append(" INNER JOIN t_delivery_sku ds");
        sql.append(" ON dd.id = ds.delivery_detail_id");
        sql.append(" AND ds.deleted_at IS NULL");

        sql.append(" INNER JOIN t_item i");
        sql.append(" ON d.part_no_id = i.id");
        sql.append(" AND i.deleted_at IS NULL");

        sql.append(" INNER JOIN t_order o");
        sql.append(" ON d.order_id = o.id");
        sql.append(" AND o.deleted_at IS NULL");
        final String mdfMakerCode = searchCondition.getMdfMakerCode();
        if (StringUtils.isNotEmpty(mdfMakerCode)) {
            sql.append(" AND o.mdf_maker_code = :mdfMakerCode");
            parameterMap.put("mdfMakerCode", mdfMakerCode);
        }

        final OnOffType lgSendFlg = searchCondition.getLgSendFlg();
        if (null != lgSendFlg && OnOffType.NO_SELECT != lgSendFlg) {
            sql.append(" INNER JOIN ");
        } else if (isCount) {
            return; // lgSendFlgが未選択の場合、以降の処理は件数取得には不要
        } else {
            sql.append(" LEFT OUTER JOIN ");
        }
        sql.append(" t_purchase p");
        sql.append(" ON dd.division_code = p.division_code");
        sql.append(" AND dd.delivery_count = p.purchase_count");
        sql.append(" AND dd.delivery_id = p.delivery_id");
        sql.append(" AND ds.color_code = p.color_code");
        sql.append(" AND ds.size = p.size");
        sql.append(" AND p.deleted_at IS NULL");
        if (null != lgSendFlg && OnOffType.NO_SELECT != lgSendFlg) {
            sql.append(" AND CASE :lgSendFlg");
            parameterMap.put("lgSendFlg", lgSendFlg.getValue());
            sql.append(" WHEN true THEN p.lg_send_type = ");
            sql.append(LgSendType.INSTRUCTION.getValue());
            sql.append(" ELSE p.lg_send_type = 0 END ");
        }

        if (isCount) {
            return; // 以降の処理は件数取得には不要
        }

        sql.append(" LEFT OUTER JOIN m_sirmst sm");
        sql.append(" ON o.mdf_maker_code = sm.sire");
        sql.append(" AND sm.deleted_at IS NULL");
        sql.append(" AND sm.mntflg IN ('1', '2', '')");

        // PRD_0008 add SIT start
        sql.append(" LEFT OUTER JOIN (");
        sql.append("  SELECT");
        sql.append("   COUNT(p.id) AS purchase_registered_count");
        sql.append("  ,p.division_code");
        sql.append("  ,p.purchase_count");
        sql.append("  ,p.order_number");
        sql.append("  FROM t_purchase p ");
        sql.append("  WHERE p.deleted_at IS NULL");
        sql.append("  GROUP BY");
        sql.append("   p.division_code");
        sql.append("  ,p.purchase_count");
        sql.append("  ,p.order_number) prc");
        sql.append(" ON  prc.division_code = dd.division_code");
        sql.append(" AND prc.purchase_count = dd.delivery_count");
        sql.append(" AND prc.order_number = d.order_number");

        sql.append(" LEFT OUTER JOIN (");
        sql.append("  SELECT");
        sql.append("   COUNT(p.id) AS purchase_confirmed_count");
        sql.append("  ,p.division_code");
        sql.append("  ,p.purchase_count");
        sql.append("  ,p.order_number");
        sql.append("  FROM t_purchase p");
        sql.append("  WHERE p.deleted_at IS NULL");
        sql.append("  AND p.lg_send_type != 0");
        sql.append("  GROUP BY");
        sql.append("   p.division_code");
        sql.append("  ,p.purchase_count");
        sql.append("  ,p.order_number) pcc");
        sql.append(" ON  pcc.division_code = dd.division_code");
        sql.append(" AND pcc.purchase_count = dd.delivery_count");
        sql.append(" AND pcc.order_number = d.order_number");
        // PRD_0008 add SIT end
    }

    /**
     * WHERE句を生成.
     * @param sql sql
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     */
    private void generateWherePhrase(final StringBuilder sql, final Map<String, Object> parameterMap, final PurchaseSearchConditionModel searchCondition) {
        final List<String> sqlColumns = new ArrayList<>();

        sqlColumns.add(" dd.deleted_at IS NULL ");
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "arrivalShop", "COALESCE(dd.arrival_place, LEFT(dd.logistics_code,1))",
                SqlQueryCriteriaType.EQUAL, shipmentComponent.extraxtLogisticsCode(searchCondition.getArrivalShop()));
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "correctionAtFrom", "dd.correction_at",
                SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO, searchCondition.getCorrectionAtFrom());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "correctionAtTo", "dd.correction_at",
                SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO, searchCondition.getCorrectionAtTo());
        QueryUtils.addSqlWhereCastInteger(sqlColumns, parameterMap, "deliveryNumberFrom", "CAST(dd.delivery_number AS UNSIGNED)",
                SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO, searchCondition.getDeliveryNumberFrom());
        QueryUtils.addSqlWhereCastInteger(sqlColumns, parameterMap, "deliveryNumberTo", "CAST(dd.delivery_number AS UNSIGNED)",
                SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO, searchCondition.getDeliveryNumberTo());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "arrivalFlg", "dd.arrival_flg",
                SqlQueryCriteriaType.EQUAL, searchCondition.getArrivalFlg());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "arrivalAtFrom", "dd.arrival_at",
                SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO, searchCondition.getArrivalAtFrom());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "arrivalAtTo", "dd.arrival_at",
                SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO, searchCondition.getArrivalAtTo());
        // PRD_0021 add SIT start
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap, "partNo", "i.part_no",
        		SqlQueryCriteriaType.LIKE_PARTIAL, searchCondition.getPartNo());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "brandCode", "i.brand_code",
        		SqlQueryCriteriaType.EQUAL, searchCondition.getBrandCode());
        // PRD_0021 add SIT end

        sql.append(" WHERE ").append(StringUtils.join(sqlColumns, " " + SqlQuerySpecificationType.AND + " "));
    }

    /**
     * GROUP BY句を生成.
     * @param sql sql
     */
    private void generateGroupByPhrase(final StringBuilder sql) {
        sql.append(" GROUP BY ");
        sql.append(" dd.delivery_id");
        sql.append(" , dd.division_code");
    }

    /**
     * ORDER BY句を生成.
     * @param sql sql
     */
    private void generateOrderByPhrase(final StringBuilder sql) {
        sql.append(" ORDER BY ");
        sql.append(" order_number ASC");
        sql.append(" , delivery_count ASC");
        sql.append(" , division_code ASC");
    }
}
