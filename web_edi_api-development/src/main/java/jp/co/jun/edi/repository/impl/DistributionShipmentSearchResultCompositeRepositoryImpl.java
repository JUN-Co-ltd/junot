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

import jp.co.jun.edi.entity.extended.ExtendedDistributionShipmentSearchResultEntity;
import jp.co.jun.edi.model.DistributionShipmentSearchConditionModel;
import jp.co.jun.edi.repository.custom.DistributionShipmentSearchResultCompositeRepositoryCustom;
import jp.co.jun.edi.type.SqlQueryCriteriaType;
import jp.co.jun.edi.type.SqlQuerySpecificationType;
import jp.co.jun.edi.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * ExtendedDistributionShipmentDeliveryStoreRepository実装クラス.
 */
@Slf4j
public class DistributionShipmentSearchResultCompositeRepositoryImpl
implements DistributionShipmentSearchResultCompositeRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 出荷指示検索実行結果を出力.
     * @param searchCondition 検索条件
     * @param pageable ページ
     * @return 出荷指示検索実行結果
     */
    @Override
    public Page<ExtendedDistributionShipmentSearchResultEntity> findBySearchCondition(
            final DistributionShipmentSearchConditionModel searchCondition,
            final Pageable pageable
            ) {
        // SELECT句を生成.
        final StringBuilder sqlSelect = new StringBuilder();
        generateSelectPhrase(sqlSelect);

        // FROM句を生成.
        final Map<String, Object> parameterMap = new HashMap<>();
        final StringBuilder sqlFrom = new StringBuilder();
        generateFromPhrase(sqlFrom);

        // WHERE句を生成.
        final StringBuilder sqlWhere = new StringBuilder();
        generateWherePhrase(sqlWhere, parameterMap, searchCondition);

        // ORDER BY句を生成.
        final StringBuilder sqlOrderBy = new StringBuilder();
        generateOrderByPhrase(sqlOrderBy);

        // 結合.
        final StringBuilder sql = new StringBuilder();
        // PRD_0012 mod SIT start
        //sql.append(sqlSelect).append(sqlFrom).append(sqlWhere);
        sql.append(sqlSelect).append(sqlFrom).append(sqlWhere).append(sqlOrderBy);
        // PRD_0012 mod SIT end

        if (log.isDebugEnabled()) {
            log.debug("sql:" + sql.toString());
        }

        // クエリにパラメータを設定.
        final Query query = entityManager.createNativeQuery(sql.toString(), ExtendedDistributionShipmentSearchResultEntity.class);
        QueryUtils.setQueryParameters(query, parameterMap);

        // 開始位置を設定.
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        // 取得件数を設定.
        query.setMaxResults(pageable.getPageSize());

        // 件数
        final long count = countAllRecord(sqlWhere, parameterMap, searchCondition);
        if (count == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, count);
        }

        @SuppressWarnings("unchecked")
        final List<ExtendedDistributionShipmentSearchResultEntity> rslt = query.getResultList();

        return new PageImpl<>(rslt, pageable, count);
    }

    /**
     * @param sqlWhere WHERE句
     * @param parameterMap パラメータ
     * @param searchCondition 検索条件
     * @return レコード件数
     */
    private long countAllRecord(
            final StringBuilder sqlWhere,
            final Map<String, Object> parameterMap,
            final DistributionShipmentSearchConditionModel searchCondition) {
        final StringBuilder sqlCount = new StringBuilder();
        final StringBuilder sqlFrom = new StringBuilder();

        sqlCount.append(" SELECT COUNT(detail.id) AS cnt ");

        generateFromPhrase(sqlFrom);
        sqlCount.append(sqlFrom).append(sqlWhere);

        if (log.isDebugEnabled()) {
            log.debug("sqlCount:" + sqlCount.toString());
        }

        final Query query = entityManager.createNativeQuery(sqlCount.toString());
        return QueryUtils.count(query, parameterMap);
    }

    /**
     * SELECT句を作成する.
     * @param sql sql
     */
    private void generateSelectPhrase(final StringBuilder sql) {
        sql.append(" SELECT ");
        sql.append(" detail.id as id");
        sql.append(" ,detail.shipping_instructions_at");
        sql.append(" ,detail.arrival_flg");
        sql.append(" ,detail.arrival_at");
        sql.append(" ,detail.delivery_request_at");
        sql.append(" ,detail.send_code");
        sql.append(" ,detail.delivery_number");
        sql.append(" ,detail.delivery_count");
        sql.append(" ,detail.division_code");
        sql.append(" ,detail.carry_type");
        sql.append(" ,base.order_number");
        sql.append(" ,base.part_no");
        sql.append(" ,detail.shipping_instructions_flg");
        sql.append(" ,ds.fix_arrival_lot_sum");
        sql.append(" ,store.delivery_lot_sum");
        sql.append(" ,store.delivery_lot_sum * code.retail_price as total_price");
        sql.append(" ,code.product_name");
    }

    /**
     * FROM句を作成する.
     * @param sql sql
     */
    private void generateFromPhrase(
            final StringBuilder sql) {
        sql.append(" FROM t_delivery_detail detail ");
        sql.append("INNER JOIN (");
        sql.append("         SELECT ");
        sql.append("         inner_store.delivery_detail_id,");
        sql.append("         IFNULL(SUM(store_sku.delivery_lot_sum),0) as delivery_lot_sum ");
        sql.append("         FROM t_delivery_store inner_store ");
        //----------------------------------
        // 得意先ごとのロット合計数を取得
        sql.append("         INNER JOIN");
        sql.append("             (");
        sql.append("                 SELECT");
//PRD_0117 mod JFE① Start--
//        sql.append("                     IFNULL(SUM(dss.delivery_lot),0) as delivery_lot_sum ");
        sql.append("                     dss.delivery_lot as delivery_lot_sum ");
//PRD_0117 mod JFE①  END --
        sql.append("                     ,dss.delivery_store_id ");
        sql.append("                 FROM ");
        sql.append("                     t_delivery_store_sku dss ");
        sql.append("                 WHERE");
        sql.append("                     1=1 ");
        sql.append("                     AND dss.deleted_at IS NULL ");
//PRD_0117 del JFE② Start--
//        sql.append("                 GROUP BY ");
//        sql.append("                     dss.delivery_store_id ");
//PRD_0117 del JFE②  END --
        sql.append("             ) store_sku  ");
        sql.append("                 ON store_sku.delivery_store_id = inner_store.id ");
        sql.append("        GROUP BY inner_store.delivery_detail_id");
        sql.append("     ) store");
        sql.append(" ON store.delivery_detail_id = detail.id");
        sql.append(" INNER JOIN (");
        sql.append("   SELECT  ");
        sql.append("     base.id");
        sql.append("     ,base.order_number");
        sql.append("     ,base.part_no");
        sql.append("     ,base.part_no_id ");
        sql.append("   FROM t_delivery base");
        sql.append("   WHERE 1=1 ");
        sql.append("   AND base.deleted_at IS NULL ");
        sql.append(" ) base");
        sql.append(" ON base.id = detail.delivery_id");

        sql.append(" INNER JOIN (");
        //----------------------------------
        // 品番/店舗コード
        // ※サブクエリで検索対象を先に絞る
        sql.append("   SELECT ");
        sql.append("     hinban.id as part_no_id");
        sql.append("     ,hinban.part_no ");
        sql.append("     ,cm.code1 as brand_code");
        sql.append("     ,cm.item4 as tmp_code");
        sql.append("     ,hinban.retail_price");
        sql.append("     ,hinban.product_name");
        sql.append("   FROM m_codmst cm");
        sql.append("   INNER JOIN t_item hinban");
        sql.append("   ON cm.code1 = hinban.brand_code");
        sql.append("   AND cm.tblId = '02'");
        sql.append("   AND hinban.deleted_at IS NULL ");
        sql.append(" ) code");
        sql.append(" ON code.part_no_id = base.part_no_id ");
        // 納品SKU
        sql.append(" INNER JOIN ");
        sql.append(" (SELECT ");
        sql.append("     inner_ds.delivery_detail_id ");
        sql.append("    ,IFNULL(SUM(inner_ds.arrival_lot),0) AS fix_arrival_lot_sum ");
        sql.append("   FROM t_delivery_sku inner_ds ");
        sql.append("    WHERE inner_ds.deleted_at IS NULL ");
        sql.append("    GROUP BY inner_ds.delivery_detail_id ");
        sql.append("  ) ds ");
        sql.append("  ON detail.id = ds.delivery_detail_id ");
    }

    /**
     * WHERE句を生成.
     * @param sql sql
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     */
    private void generateWherePhrase(
            final StringBuilder sql,
            final Map<String, Object> parameterMap,
            final DistributionShipmentSearchConditionModel searchCondition) {

        final List<String> sqlColumns = new ArrayList<>();

        sqlColumns.add(" detail.deleted_at IS NULL");

        // a)　仕入確定済かつ、店舗配分が完了している納品依頼の情報を表示する。
        // t_delivery_detail.store_registered_flg = true
        sqlColumns.add(" detail.store_registered_flg = true");
        sqlColumns.add(" detail.arrival_flg = true");

        // 期間はnull or date で入ってくるため、Emptyの判定不要
        QueryUtils.addSqlWhere(sqlColumns, parameterMap,
                "shippingAtFrom",
                "detail.shipping_instructions_at",
                SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO, searchCondition.getShippingAtFrom());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap,
                "shippingAtTo",
                "detail.shipping_instructions_at",
                SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO, searchCondition.getShippingAtTo());

        /* PRD_0005 add SIT start */
        // 入荷日
        QueryUtils.addSqlWhere(sqlColumns, parameterMap,
            "arrivalAtFrom",
            "detail.arrival_at",
            SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO, searchCondition.getArrivalAtFrom());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap,
            "arrivalAtTo",
            "detail.arrival_at",
            SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO, searchCondition.getArrivalAtTo());
        /* PRD_0005 add SIT end */

        /* PRD_0004 add SIT start */
        // 納品依頼日
        QueryUtils.addSqlWhere(sqlColumns, parameterMap,
            "deliveryRequestAtFrom",
            "detail.delivery_request_at",
            SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO, searchCondition.getDeliveryRequestAtFrom());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap,
            "deliveryRequestAtTo",
            "detail.delivery_request_at",
            SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO, searchCondition.getDeliveryRequestAtTo());
        /* PRD_0004 add SIT end */


        // ディスタ(shpCd-> 5,6桁目の物流コードで絞り込みを実行)
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap,
                "shpcd",
                "detail.logistics_code",
                SqlQueryCriteriaType.EQUAL, searchCondition.getShpcd());
        // 事業部コード
        String departmentCode = searchCondition.getDepartmentCode();
        if (StringUtils.isNotEmpty(departmentCode)) {
            QueryUtils.addSqlWhereString(sqlColumns, parameterMap,
                    "departmentCode",
                    "code.tmp_code",
                    SqlQueryCriteriaType.EQUAL, departmentCode);
        }
        // 課コード
        String divisionCode = searchCondition.getDivisionCode();
        if (StringUtils.isNotEmpty(divisionCode)) {
            QueryUtils.addSqlWhereString(sqlColumns, parameterMap,
                    "divisionCode",
                    "detail.division_code",
                    SqlQueryCriteriaType.EQUAL, divisionCode);
        }
        // ブランドコード/アイテムコード
        String likePartNo = "";
        String brandCode = searchCondition.getBrandCode();
        if (StringUtils.isNotEmpty(brandCode)) {
            likePartNo = likePartNo + brandCode;
        } else {
            likePartNo = likePartNo + "__";
        }
        String itemCode = searchCondition.getItemCode();
        if (StringUtils.isNotEmpty(itemCode)) {
            likePartNo = likePartNo + itemCode;
        }
        // 前方一致
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap,
                "likePartNo",
                "base.part_no",
                SqlQueryCriteriaType.LIKE_FORWARD, likePartNo);

        if (!sqlColumns.isEmpty()) {
            sql.append(" WHERE ").append(StringUtils.join(sqlColumns, " " + SqlQuerySpecificationType.AND + " "));
        }
    }
    /**
     * ORDER BY句を生成.
     * @param sql sql
     */
    private void generateOrderByPhrase(final StringBuilder sql) {
        sql.append(" ORDER BY ");
        // PRD_0012 mod SIT start
        //sql.append(" id ASC");
        sql.append(" arrival_at DESC");
        sql.append(",id ASC");
        // PRD_0012 mod SIT end
    }
}
