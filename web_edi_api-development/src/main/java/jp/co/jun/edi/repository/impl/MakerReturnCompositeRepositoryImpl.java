package jp.co.jun.edi.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.component.ShipmentComponent;
import jp.co.jun.edi.entity.MakerReturnCompositeEntity;
import jp.co.jun.edi.model.MakerReturnSearchResultModel;
import jp.co.jun.edi.repository.custom.MakerReturnCompositeRepositoryCustom;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.SqlQueryCriteriaType;
import jp.co.jun.edi.type.SqlQuerySpecificationType;
import jp.co.jun.edi.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * メーカー返品一覧Repository実装クラス.
 */
@Slf4j
public class MakerReturnCompositeRepositoryImpl implements MakerReturnCompositeRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ShipmentComponent shipmentComponent;

    /**
     * メーカー返品一覧を取得する.
     *
     * SELECT
     *      mr.order_id AS order_id,
     *      mr.voucher_number AS voucher_number,
     *      mr.lg_send_type AS lg_send_type,
     *      mr.return_at AS return_at,
     *      mr.supplier_code AS supplier_code,
     *      ms.name AS name,
     *      SUM(mr.return_lot) AS return_lot,
     *          CASE
     *              when o.non_conforming_product_unit_price > 0 then o.non_conforming_product_unit_price
     *          else o.unit_price
     *          end AS unit_price,
     *      mr.order_number AS order_number,
     *      mr.created_at AS created_at
     * FROM t_maker_return mr
     *      LEFT OUTER JOIN m_sirmst ms
     *          ON mr.supplier_code = ms.sire
     *          AND ms.deleted_at IS NULL
     *      INNER JOIN t_order o
     *          ON mr.order_id = o.id
     *          AND o.deleted_at IS NULL
     * WHERE
     *      mr.deleted_at IS NULL
     *      AND mr.logistics_code = shpcd
     *      AND mr.mdf_staff_code = :mdfStaffCod
     *      AND mr.supplier_code = :supplierCode
     *      AND mr.lg_send_type = :lgSendType
     *      AND :voucherNumberFrom <= mr.voucher_number
     *      AND mr.voucher_number <= :voucherNumberTo
     *      AND :voucherNumberInputAtFrom <= mr.created_at
     *      AND mr.created_at <= :voucherNumberInputAtTo
     *      AND :voucherNumberAtFrom <= mr.return_at
     *      AND mr.return_at <= :voucherNumberAtTo
     * GROUP BY mr.order_id,mr.voucher_number
     * ORDER BY mr.voucher_number DESC
     */

    @Override
    public Page<MakerReturnCompositeEntity> findBySearchCondition(
            final MakerReturnSearchResultModel searchCondition,
            final Pageable pageable) {

        // 検索
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
        generateFromPhrase(sqlFrom, searchCondition, parameterMap);

        sql.append(sqlFrom).append(sqlWhere);

        generateGroupByPhrase(sql);
        generateOrderByPhrase(sql);

        if (log.isDebugEnabled()) {
            log.debug("sql:" + sql.toString());
        }

        final Query query = entityManager.createNativeQuery(sql.toString(), MakerReturnCompositeEntity.class);

        // クエリにパラメータを設定
        QueryUtils.setQueryParameters(query, parameterMap);

        // 開始位置を設定
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        // 取得件数を設定
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        final List<MakerReturnCompositeEntity> result = query.getResultList();

        return new PageImpl<>(result, pageable, count);

    }

    /**
     * @param sqlWhere WHERE句
     * @param parameterMap パラメータ
     * @param searchCondition 検索条件
     * @return レコード件数
     */
    private long countAllRecord(final StringBuilder sqlWhere, final Map<String, Object> parameterMap,
            final MakerReturnSearchResultModel searchCondition) {
        final StringBuilder sqlCount = new StringBuilder();
        final StringBuilder sqlFrom = new StringBuilder();

        sqlCount.append(" SELECT COUNT(rslt.cnt)");
        sqlCount.append(" FROM (SELECT COUNT(mr.order_id) AS cnt");

        generateFromPhrase(sqlFrom, searchCondition, parameterMap);
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

        sqlColumns.add("mr.order_id AS order_id");
        sqlColumns.add("mr.voucher_number AS voucher_number");
        sqlColumns.add("mr.lg_send_type AS lg_send_type");
        sqlColumns.add("mr.return_at AS return_at");
        sqlColumns.add("mr.supplier_code AS supplier_code");
        sqlColumns.add("ms.name AS name");
        sqlColumns.add("SUM(mr.return_lot) AS return_lot");
        sqlColumns.add("CASE"
                + "     WHEN o.non_conforming_product_unit_price > 0 then o.non_conforming_product_unit_price"
                + "     ELSE o.unit_price"
                + "     END AS unit_price");
        sqlColumns.add("mr.order_number AS order_number");
        sqlColumns.add("mr.created_at AS created_at");

        sql.append("SELECT ").append(StringUtils.join(sqlColumns, ", "));
    }

    /**
     * FROM句を生成.
     * @param sql sql
     * @param searchCondition 検索条件
     * @param parameterMap パラメーターマップ
     */
    private void generateFromPhrase(final StringBuilder sql,
            final MakerReturnSearchResultModel searchCondition,
            final Map<String, Object> parameterMap) {

        sql.append(" FROM t_maker_return mr");

        sql.append("    LEFT OUTER JOIN m_sirmst ms");
        sql.append("         ON mr.supplier_code = ms.sire");
        sql.append("         AND ms.deleted_at IS NULL");

        sql.append("    INNER JOIN t_order o");
        sql.append("         ON mr.order_id = o.id");
        sql.append("         AND o.deleted_at IS NULL");

    }

    /**
     * WHERE句を生成.
     * @param sql sql
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     */
    private void generateWherePhrase(final StringBuilder sql, final Map<String, Object> parameterMap,
            final MakerReturnSearchResultModel searchCondition) {

        final List<String> sqlColumns = new ArrayList<>();
        final LgSendType lgSendType = searchCondition.getLgSendType();

        sqlColumns.add(" mr.deleted_at IS NULL ");
        /** 店舗コード(ディスタ選択). */
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "shpcd", "mr.logistics_code",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(shipmentComponent.extraxtOldLogisticsCode(searchCondition.getShpcd()), null));
        /** メーカー . */
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "mdfStaffCode", "mr.mdf_staff_code",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getMdfStaffCode(), null));
        /** 担当者 . */
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "supplierCode", "mr.supplier_code",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getSupplierCode(), null));
        /** 状態(送信) . */
        if (Objects.nonNull(lgSendType) && LgSendType.NO_SELECT != lgSendType) {
            QueryUtils.addSqlWhere(sqlColumns, parameterMap, "lgSendType", "mr.lg_send_type",
                SqlQueryCriteriaType.EQUAL, lgSendType.getValue());
        }
        /** 伝票番号from. */
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "voucherNumberFrom", "mr.voucher_number",
                SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO, StringUtils.defaultIfEmpty(searchCondition.getVoucherNumberFrom(), null));
        /** 伝票番号to. */
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "voucherNumberTo", "mr.voucher_number",
                SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO, StringUtils.defaultIfEmpty(searchCondition.getVoucherNumberTo(), null));
        /** 伝票入力日from. */
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "voucherNumberInputAtFrom", "mr.created_at",
                SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO, searchCondition.getVoucherNumberInputAtFrom());
        /** 伝票入力日to. */
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "voucherNumberInputAtTo", "mr.created_at",
                SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO, searchCondition.getVoucherNumberInputAtTo());
        /** 伝票日付from . */
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "voucherNumberAtFrom", "mr.return_at",
                SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO, searchCondition.getVoucherNumberAtFrom());
        /** 伝票日付to . */
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "voucherNumberAtTo", "mr.return_at",
                SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO, searchCondition.getVoucherNumberAtTo());

        sql.append(" WHERE ").append(StringUtils.join(sqlColumns, " " + SqlQuerySpecificationType.AND + " "));
    }

    /**
     * GROUP BY句を生成.
     * @param sql sql
     */
    private void generateGroupByPhrase(final StringBuilder sql) {
        sql.append(" GROUP BY ");
        sql.append(" mr.order_id");
        sql.append(" , mr.voucher_number");
    }

    /**
     * ORDER BY句を生成.
     * @param sql sql
     */
    private void generateOrderByPhrase(final StringBuilder sql) {
        sql.append(" ORDER BY ");
        sql.append(" mr.voucher_number DESC");
    }
}
