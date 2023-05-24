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
import jp.co.jun.edi.entity.extended.ExtendedTInventoryShipmentSearchResultEntity;
import jp.co.jun.edi.model.InventoryShipmentSearchConditionModel;
import jp.co.jun.edi.repository.custom.InventoryShipmentSearchResultCompositeRepositoryCustom;
import jp.co.jun.edi.type.SqlQueryCriteriaType;
import jp.co.jun.edi.type.SqlQuerySpecificationType;
import jp.co.jun.edi.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * InventoryShipmentSearchResultCompositeRepository実装クラス.
 */
@Slf4j
public class InventoryShipmentSearchResultCompositeRepositoryImpl
implements InventoryShipmentSearchResultCompositeRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ShipmentComponent shipmentComponent;

    /**
     * 拡張在庫出荷指示情報検索実行結果を出力.
     * @param searchCondition 検索条件
     * @param pageable ページ
     * @return 拡張在庫出荷指示検索実行結果
     */
    @Override
    public Page<ExtendedTInventoryShipmentSearchResultEntity> findBySearchCondition(
            final InventoryShipmentSearchConditionModel searchCondition,
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

        // GROUP BY句を生成.
        final StringBuilder sqlGroupBy = new StringBuilder();
        generateGroupByPhrase(sqlGroupBy);

        // ORDER BY句を生成.
        final StringBuilder sqlOrderBy = new StringBuilder();
        generateOrderByPhrase(sqlOrderBy);

        // 結合.
        final StringBuilder sql = new StringBuilder();
        sql.append(sqlSelect).append(sqlFrom).append(sqlWhere).append(sqlGroupBy).append(sqlOrderBy);

        if (log.isDebugEnabled()) {
            log.debug("sql:" + sql.toString());
        }

        // クエリにパラメータを設定.
        final Query query = entityManager.createNativeQuery(sql.toString(),
                ExtendedTInventoryShipmentSearchResultEntity.class);
        QueryUtils.setQueryParameters(query, parameterMap);

        // 開始位置を設定.
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        // 取得件数を設定.
        query.setMaxResults(pageable.getPageSize());

        // 件数
        final long count = countAllRecord(sql, parameterMap, searchCondition);
        if (count == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, count);
        }

        @SuppressWarnings("unchecked")
        final List<ExtendedTInventoryShipmentSearchResultEntity> rslt = query.getResultList();

        return new PageImpl<>(rslt, pageable, count);
    }

    /**
     * @param sql SQL文
     * @param parameterMap パラメータ
     * @param searchCondition 検索条件
     * @return レコード件数
     */
    private long countAllRecord(
            final StringBuilder sql,
            final Map<String, Object> parameterMap,
            final InventoryShipmentSearchConditionModel searchCondition) {
        final StringBuilder sqlCount = new StringBuilder();
        final StringBuilder sqlFrom = new StringBuilder();

        sqlCount.append(" SELECT COUNT(a.instructor_system) AS cnt ");
        sqlFrom.append(" FROM (" + sql + ") a");
        sqlCount.append(sqlFrom);

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
        sql.append("   tis.cargo_at ");
        sql.append(" , tis.cargo_place ");
        sql.append(" , tis.instructor_system ");
        sql.append(" , tis.part_no  AS convert_brand_code ");
        sql.append(" , cm.item1 AS brand_name ");
        sql.append(" , jun.hka AS division_code ");
        sql.append(" , tis.part_no ");
        sql.append(" , item.product_name ");
        // データの取りまとめた出荷指示数の合計
        sql.append(" , SUM(tis.shipping_instruction_lot) AS delivery_lot_sum ");
        // データの取りまとめた[出荷指示数×上代]の合計
        sql.append(" , SUM(tis.shipping_instruction_lot * tis.retail_price) AS retail_price_sum ");
        sql.append(" , tis.lg_send_type ");
    }

    /**
     * FROM句を作成する.
     * @param sql sql
     */
    private void generateFromPhrase(final StringBuilder sql) {
        // テーブルの取得も変更
        sql.append(" FROM t_inventory_shipment tis ");
        sql.append(" LEFT JOIN t_item item ");
        sql.append("   ON  tis.part_no = item.part_no ");
        sql.append("   AND item.deleted_at IS NULL ");
        sql.append(" LEFT JOIN m_junmst jun ");
        sql.append("   ON  SUBSTRING(tis.part_no,1,2) = jun.brand ");
        sql.append("   AND jun.shpcd =  IFNULL((SELECT cm2.code2 FROM m_codmst cm2 ");
        sql.append("                             WHERE cm2.tblId = '49'");
        sql.append("                               AND TRIM(cm2.item1) = tis.shop_code");
        sql.append("                               AND cm2.code1 = CONCAT('0',tis.instructor_system)");
        sql.append("                               AND cm2.mntflg IN ('1', '2', '') ");
        sql.append("                               AND cm2.deleted_at IS NULL");
        sql.append("                           ) , tis.shop_code)");
        sql.append("   AND jun.deleted_at IS NULL ");
        sql.append(" LEFT JOIN m_codmst cm ");
        sql.append("   ON SUBSTRING(tis.part_no,1,2) = cm.code1 ");
        sql.append("   AND cm.tblId = '02' ");
        sql.append("   AND cm.mntflg IN ('1', '2', '') ");
        sql.append("   AND cm.deleted_at IS NULL ");
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
            final InventoryShipmentSearchConditionModel searchCondition) {
        final List<String> sqlColumns = new ArrayList<>();
        sqlColumns.add(" tis.deleted_at IS NULL");

        // 未送信のみ表示
        sqlColumns.add(" tis.lg_send_type = 0");

        // 期間はnull or date で入ってくるため、Emptyの判定不要
        QueryUtils.addSqlWhere(sqlColumns, parameterMap,
                "cargoAtFrom",
                "tis.cargo_at",
                SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO,
                searchCondition.getCargoAtFrom());
        QueryUtils.addSqlWhere(sqlColumns, parameterMap,
                "cargoAtTo",
                "tis.cargo_at",
                SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO,
                searchCondition.getCargoAtTo());
        // ディスタ
        QueryUtils.addSqlWhereString(sqlColumns, parameterMap,
                "shopCode",
                "tis.cargo_place",
                SqlQueryCriteriaType.EQUAL,
                shipmentComponent.extraxtLogisticsCode(searchCondition.getShpcd()));

        // 指示元システム
        QueryUtils.addSqlWhere(sqlColumns, parameterMap,
                "instructorSystem",
                "tis.instructor_system",
                SqlQueryCriteriaType.EQUAL, searchCondition.getInstructorSystem().getValue());
        // 事業部コード
        final String departmentCode = searchCondition.getDepartmentCode();
        if (StringUtils.isNotEmpty(departmentCode)) {
            QueryUtils.addSqlWhereString(sqlColumns, parameterMap,
                    "departmentCode",
                    "cm.item4",
                    SqlQueryCriteriaType.EQUAL, departmentCode);
        }
        // 課コード
        final String divisionCode = searchCondition.getDivisionCode();
        if (StringUtils.isNotEmpty(divisionCode)) {
            QueryUtils.addSqlWhereString(sqlColumns, parameterMap,
                    "divisionCode",
                    "jun.hka",
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
                "tis.part_no",
                SqlQueryCriteriaType.LIKE_FORWARD, likePartNo);


        if (!sqlColumns.isEmpty()) {
            sql.append(" WHERE ").append(StringUtils.join(sqlColumns,
                    " " + SqlQuerySpecificationType.AND + " "));
        }
    }

    /**
     * GROUP BY句を生成.
     * @param sql sql
     */
    private void generateGroupByPhrase(final StringBuilder sql) {
        sql.append(" GROUP BY ");
        sql.append("   tis.cargo_at ");
        sql.append(" , tis.instructor_system ");
        sql.append(" , jun.hka ");
        sql.append(" , tis.part_no ");
    }
    /**
     * ORDER BY句を生成.
     * @param sql sql
     */
    private void generateOrderByPhrase(final StringBuilder sql) {
        sql.append(" ORDER BY ");
        sql.append("   tis.cargo_at ");
        sql.append(" , tis.instructor_system ");
        sql.append(" , jun.hka ");
        sql.append(" , tis.part_no ");
    }
}
