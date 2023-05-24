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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.extended.ExtendedTItemListEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.ItemSearchConditionModel;
import jp.co.jun.edi.repository.custom.ItemCompositeRepositoryCustom;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.OrderCategoryType;
import jp.co.jun.edi.type.SqlQueryCriteriaType;
import jp.co.jun.edi.type.SqlQuerySpecificationType;
import jp.co.jun.edi.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 品番情報ListRepository実装クラス.
 */
@Slf4j
public class ItemCompositeRepositoryImpl implements ItemCompositeRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".repository";

    @Value("${" + PROPERTY_NAME_PREFIX + ".specification.extended-titem-list-specification.keyword-conditions-limit-size}")
    private int keywordConditionsLimitSize;

    private static final String SELECT_PHRASE_SQL =
            "SELECT i.id as id"
                    + "     ,i.part_no as part_no"
                    + "     ,i.preferred_delivery_date as preferred_delivery_date"
                    + "     ,i.delivery_date as delivery_date"
                    + "     ,i.provi_order_date as provi_order_date"
                    + "     ,i.deployment_date as deployment_date"
                    + "     ,i.deployment_week as deployment_week"
                    + "     ,i.p_end_date as p_end_date"
                    + "     ,i.p_end_week as p_end_week"
                    + "     ,i.product_name as product_name"
                    + "     ,i.product_name_kana as product_name_kana"
                    + "     ,i.year as year"
                    + "     ,i.season_code as season_code"
                    + "     ,i.sub_season_code as sub_season_code"
                    + "     ,i.current_product_order_supplier_id as current_product_order_supplier_id"
                    + "     ,i.coo_code as coo_code"
                    + "     ,i.retail_price as retail_price"
                    + "     ,i.matl_cost as matl_cost"
                    + "     ,i.processing_cost as processing_cost"
                    + "     ,i.accessories_cost as accessories_cost"
                    + "     ,i.other_cost as other_cost"
                    + "     ,i.planner_code as planner_code"
                    + "     ,i.mdf_staff_code as mdf_staff_code"
                    + "     ,i.pataner_code as pataner_code"
                    + "     ,i.pattern_no as pattern_no"
                    + "     ,i.marui_dept_brand as marui_dept_brand"
                    + "     ,i.marui_garment_no as marui_garment_no"
                    + "     ,i.voi_code as voi_code"
                    + "     ,i.material_code as material_code"
                    + "     ,i.zone_code as zone_code"
                    + "     ,i.brand_code as brand_code"
                    + "     ,i.sub_brand_code as sub_brand_code"
                    + "     ,i.brand_sort_code as brand_sort_code"
                    + "     ,i.dept_code as dept_code"
                    + "     ,i.item_code as item_code"
                    + "     ,i.taste_code as taste_code"
                    + "     ,i.type1_code as type1_code"
                    + "     ,i.type2_code as type2_code"
                    + "     ,i.type3_code as type3_code"
                    + "     ,i.grab_bag as grab_bag"
                    + "     ,i.inventory_management_type as inventory_management_type"
                    + "     ,i.devaluation_type as devaluation_type"
                    + "     ,i.reduced_tax_rate_flg as reduced_tax_rate_flg"
                    + "     ,i.digestion_commission_type as digestion_commission_type"
                    + "     ,i.outlet_code as outlet_code"
                    + "     ,i.maker_garment_no as maker_garment_no"
                    + "     ,i.memo as memo"
                    + "     ,i.item_massage_display as item_massage_display"
                    + "     ,i.item_massage as item_massage"
                    + "     ,i.regist_status as regist_status"
                    + "     ,i.sample as sample"
                    + "     ,i.misleading_representation as misleading_representation"
                    + "     ,i.quality_composition_status as quality_composition_status"
                    + "     ,i.quality_coo_status as quality_coo_status"
                    + "     ,i.quality_harmful_status as quality_harmful_status"
                    + "     ,i.jan_type as jan_type"
                    + "     ,i.stopped as stopped"
                    + "     ,i.junpc_tanto as junpc_tanto"
                    + "     ,i.linking_status as linking_status"
                    + "     ,i.linked_at as linked_at"
                    + "     ,i.created_at as created_at"
                    + "     ,i.created_user_id as created_user_id"
                    + "     ,i.updated_at as updated_at"
                    + "     ,i.updated_user_id as updated_user_id"
                    + "     ,i.deleted_at as deleted_at"
                    + "     ,os.supplier_code as mdf_maker_code"
                    + "     ,os.supplier_factory_code as mdf_maker_factory_code"
                    + "     ,os.consignment_factory as consignment_factory"
                    + "     ,os.supplier_staff_id as mdf_maker_staff_id"
                    + "     ,ms.name as mdf_maker_name"
                    + "     ,mk.name as mdf_maker_factory_name";

    private static final String EXISTS_STAFF_SUB_QUERY =
            "    SELECT cm.code1"
            + "      FROM m_codmst cm"
            + "     WHERE cm.tblid = :tblid ";

    private static final String EXISTS_SUPPLIER_STAFF_SUB_QUERY =
            "    SELECT mu.id"
            + "      FROM m_user mu"
            + "     WHERE";

    @Override
    public Page<ExtendedTItemListEntity> findBySpec(final ItemSearchConditionModel searchCondition,
                                                     final CustomLoginUser loginUser,
                                                     final Pageable pageable) {

        // SELECT COUNT句を生成
        final StringBuilder sqlCount = new StringBuilder();
        generateSelectCountPhrase(sqlCount);

        // FROM句を生成
        final StringBuilder sqlFrom = new StringBuilder();
        generateFromPhrase(sqlFrom, loginUser);

        // WHERE句を生成
        final Map<String, Object> parameterMap = new HashMap<>();
        final StringBuilder sqlWhere = new StringBuilder();
        generateWherePhrase(sqlWhere, parameterMap, searchCondition);

        // FROM句とWHERE句を追加
        sqlCount.append(sqlFrom).append(sqlWhere);

        if (log.isDebugEnabled()) {
            log.debug("sqlCount:" + sqlCount.toString());
        }

        // 件数を取得
        final long count = count(sqlCount.toString(), parameterMap);

        if (count == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, count);
        }

        // SELECT句を生成
        final StringBuilder sql = new StringBuilder();
        generateSelectPhrase(sql);

        // FROM句とWHERE句を追加
        sql.append(sqlFrom).append(sqlWhere);

        // ORDER BY句を生成
        generateOrderByPhrase(sql);

        final Query query = entityManager.createNativeQuery(sql.toString(), ExtendedTItemListEntity.class);

        // クエリにパラメータを設定
        setQueryParameters(query, parameterMap);

        // 開始位置を設定
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        // 取得件数を設定
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        final List<ExtendedTItemListEntity> list = query.getResultList();

        return new PageImpl<>(list, pageable, count);

    }

    /**
     * SELECT句を作成する.
     * @param sql sql
     */
    private void generateSelectPhrase(final StringBuilder sql) {
        sql.append(SELECT_PHRASE_SQL);
    }

    /**
     * FROM句を作成する.
     * @param sql sql
     * @param loginUser CustomLoginUser
     */
    private void generateFromPhrase(final StringBuilder sql, final CustomLoginUser loginUser) {
        sql.append(" FROM");
        sql.append(" t_item i");
        sql.append(" INNER JOIN t_order_supplier os");

        // 生産メーカー
        if (loginUser.isAffiliation()) {
            // JUN権限だったら発注先メーカーID(最新製品)で結合
            sql.append(" ON i.current_product_order_supplier_id = os.id");
        } else {
            // メーカー権限だったら品番とメーカーコードで結合
            sql.append(" ON i.id = os.part_no_id");
            sql.append(" AND os.supplier_code = '" + loginUser.getCompany() + "'");
        }
        sql.append(" AND os.order_category_type = '" + OrderCategoryType.PRODUCT.getValue() + "'");
        sql.append(" AND os.deleted_at IS NULL");

        // 仕入先マスタ(生産メーカー)
        sql.append(" LEFT OUTER JOIN m_sirmst ms");
        sql.append(" ON ms.sire = os.supplier_code");
        sql.append(" AND ms.deleted_at IS NULL");
        sql.append(" AND ms.mntflg IN ('1', '2', '')");

        // 工場マスタ
        sql.append(" LEFT OUTER JOIN m_kojmst mk");
        sql.append(" ON mk.sire = os.supplier_code");
        sql.append(" AND mk.kojcd = os.supplier_factory_code");
        sql.append(" AND mk.deleted_at IS NULL");
        sql.append(" AND mk.mntflg IN ('1', '2', '')");

    }

    /**
     * WHERE句を作成する.
     * @param sql sql
     * @param parameterMap パラメーターマップ
     * @param searchCondition 検索条件
     */
    private void generateWherePhrase(
            final StringBuilder sql,
            final Map<String, Object> parameterMap,
            final ItemSearchConditionModel searchCondition) {
        final List<String> sqlCalamus = new ArrayList<>();

        sqlCalamus.add("i.deleted_at IS NULL");

        // キーワード検索
        keywordContains(sqlCalamus, parameterMap, sql, searchCondition);

        // シーズン検索：完全一致
        addSqlWhereString(sqlCalamus, parameterMap, "subSeasonCode", "i.sub_season_code", SqlQueryCriteriaType.EQUAL, searchCondition.getSubSeasonCode());

        // 年度：完全一致
        final String strYear = jp.co.jun.edi.util.StringUtils.defaultString(searchCondition.getYear());
        if (StringUtils.isNotBlank(strYear)) {
            QueryUtils.addSqlWhereString(sqlCalamus, parameterMap, "year", "i.year", SqlQueryCriteriaType.EQUAL, strYear);
        }

        // 担当者(OR結合)
        staffContains(sqlCalamus, parameterMap, sql, searchCondition);

        if (!sqlCalamus.isEmpty()) {
            sql.append(" WHERE ").append(StringUtils.join(sqlCalamus, " " + SqlQuerySpecificationType.AND + " "));
        }
    }

    /**
     * キーワード選択.
     *
     * @param sqlCalamus List<String>
     * @param parameterMap Map<String, Object>
     * @param sql StringBuilder
     * @param searchCondition ItemSearchConditionModel
     */
    private void keywordContains(final List<String> sqlCalamus, final Map<String, Object> parameterMap,
                                 final StringBuilder sql, final ItemSearchConditionModel searchCondition) {

        final List<String> sqlKeywordCalamus = new ArrayList<>();

        // スペース区切り
        final List<String> partNoList = getSplitConditions(searchCondition.getPartNo());
        final List<String> productNameList = getSplitConditions(searchCondition.getProductName());
        final List<String> brandCodeList = getSplitConditions(searchCondition.getBrandCode());
        final List<String> itemCodeList = getSplitConditions(searchCondition.getItemCode());

        if (!partNoList.isEmpty()) {
            // 品番：部分一致
            QueryUtils.addSqlWhereStringList(sqlKeywordCalamus, parameterMap, "partNoList", "i.part_no",
                                               SqlQueryCriteriaType.LIKE_PARTIAL, partNoList);
        }
        if (!productNameList.isEmpty()) {
            // 品名：部分一致
            QueryUtils.addSqlWhereStringList(sqlKeywordCalamus, parameterMap, "productNameList", "i.product_name",
                                               SqlQueryCriteriaType.LIKE_PARTIAL, productNameList);
        }
        if (!brandCodeList.isEmpty()) {
            // ブランドコード：部分一致
            QueryUtils.addSqlWhereStringList(sqlKeywordCalamus, parameterMap, "brandCodeList", "i.brand_code",
                    SqlQueryCriteriaType.LIKE_PARTIAL, brandCodeList);
        }
        if (!itemCodeList.isEmpty()) {
            // アイテムコード：部分一致
            QueryUtils.addSqlWhereStringList(sqlKeywordCalamus, parameterMap, "itemCodeList", "i.item_code",
                    SqlQueryCriteriaType.LIKE_PARTIAL, itemCodeList);
        }

        if (!sqlKeywordCalamus.isEmpty()) {
            sqlCalamus.add(StringUtils.join(sqlKeywordCalamus, " " + SqlQuerySpecificationType.OR + " "));
        }

    }

    /**
     * 担当者検索.
     * @param sqlCalamus List<String>
     * @param parameterMap Map<String, Object>
     * @param sql StringBuilder
     * @param searchCondition ItemSearchConditionModel
     */
    private void staffContains(final List<String> sqlCalamus, final Map<String, Object> parameterMap,
                                 final StringBuilder sql, final ItemSearchConditionModel searchCondition) {

        final List<String> sqlStaffSqls = new ArrayList<>();
        final List<String> sqlStaffSubCalamus = new ArrayList<>();

        if (StringUtils.isNotEmpty(searchCondition.getMdfStaffName())) {

            sqlStaffSubCalamus.clear();

            parameterMap.put("tblid", MCodmstTblIdType.STAFF.getValue());

            // 製造担当
            addSqlWhereString(sqlStaffSubCalamus, parameterMap, "mdf_staff_code", "cm.code1",
                              SqlQueryCriteriaType.LIKE_FORWARD, searchCondition.getMdfStaffName());
            addSqlWhereString(sqlStaffSubCalamus, parameterMap, "mdf_staff_name", "cm.item2",
                              SqlQueryCriteriaType.LIKE_PARTIAL, searchCondition.getMdfStaffName());

            sqlStaffSqls.add(staffSubQuery("i.mdf_staff_code", sqlStaffSubCalamus));

        }
        if (StringUtils.isNotEmpty(searchCondition.getPatanerName())) {

            sqlStaffSubCalamus.clear();

            parameterMap.put("tblid", MCodmstTblIdType.STAFF.getValue());

            // パタンナー
            addSqlWhereString(sqlStaffSubCalamus, parameterMap, "pataner_code", "cm.code1",
                              SqlQueryCriteriaType.LIKE_FORWARD, searchCondition.getPatanerName());
            addSqlWhereString(sqlStaffSubCalamus, parameterMap, "pataner_name", "cm.item2",
                              SqlQueryCriteriaType.LIKE_PARTIAL, searchCondition.getPatanerName());

            sqlStaffSqls.add(staffSubQuery("i.pataner_code", sqlStaffSubCalamus));

        }
        if (StringUtils.isNotEmpty(searchCondition.getPlannerName())) {

            sqlStaffSubCalamus.clear();

            parameterMap.put("tblid", MCodmstTblIdType.STAFF.getValue());

            // 企画担当
            addSqlWhereString(sqlStaffSubCalamus, parameterMap, "planner_code", "cm.code1",
                              SqlQueryCriteriaType.LIKE_FORWARD, searchCondition.getPlannerName());
            addSqlWhereString(sqlStaffSubCalamus, parameterMap, "planner_name", "cm.item2",
                              SqlQueryCriteriaType.LIKE_PARTIAL, searchCondition.getPlannerName());

            sqlStaffSqls.add(staffSubQuery("i.planner_code", sqlStaffSubCalamus));

        }
        if (StringUtils.isNotEmpty(searchCondition.getMdfMakerStaffName())) {

            sqlStaffSubCalamus.clear();

            // 生産メーカー担当
            addSqlWhereString(sqlStaffSubCalamus, parameterMap, "supplier_staff_id", "os.supplier_staff_id",
                              SqlQueryCriteriaType.LIKE_PARTIAL, searchCondition.getMdfMakerStaffName());
            addSqlWhereString(sqlStaffSubCalamus, parameterMap, "supplier_staff_name", "mu.name",
                              SqlQueryCriteriaType.LIKE_PARTIAL, searchCondition.getMdfMakerStaffName());

            sqlStaffSqls.add(supplierStaffSubQuery("i.planner_code", sqlStaffSubCalamus));

        }

        if (!sqlStaffSqls.isEmpty()) {
            sqlCalamus.add("(" + StringUtils.join(sqlStaffSqls, " " + SqlQuerySpecificationType.OR + " ") + ")");
        }
    }

     /**
      * 担当者検索のサブクエリ.
      * @param calamu 検索対象のカラム
      * @param sqlStaffSubCalamus 検索条件
      * @return sql
      */
    private String staffSubQuery(final String calamu, final List<String> sqlStaffSubCalamus) {

        return "exists (" + EXISTS_STAFF_SUB_QUERY
                + SqlQuerySpecificationType.AND + " "
                + calamu + " = cm.code1 "
                + SqlQuerySpecificationType.AND
                + "(" +  StringUtils.join(sqlStaffSubCalamus, " "
                + SqlQuerySpecificationType.OR + " ") + "))";
    }

    /**
     * メーカー担当者検索のサブクエリ.
     * @param calamu 検索対象のカラム
     * @param sqlStaffSubCalamus 検索条件
     * @return sql
     */
   private String supplierStaffSubQuery(final String calamu, final List<String> sqlStaffSubCalamus) {

       return "exists (" + EXISTS_SUPPLIER_STAFF_SUB_QUERY
               + "(" +  StringUtils.join(sqlStaffSubCalamus, " "
               + SqlQuerySpecificationType.OR + " ") + "))";
   }

    /**
     * ORDER BY句を作成する.
     * @param sql sql
     */
    private void generateOrderByPhrase(final StringBuilder sql) {
        sql.append(" ORDER BY");
        sql.append(" i.id ASC");
    }

    /**
     * SQLにWHERE句のカラムを追加する.
     * @param sqlCalamus SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param criteria 比較演算子（"=", "LIKE"）
     * @param value 値のリスト
     */
    private void addSqlWhereString(
            final List<String> sqlCalamus,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final String value) {
        if (StringUtils.isNotEmpty(value)) {
            sqlCalamus.add(columnName + " " + criteria.getValue() + " :" + key);

            switch (criteria) {
            case EQUAL:
                // 完全一致
                parameterMap.put(key, value);
                break;

            case LIKE_FORWARD:
                // 前方一致
                parameterMap.put(key, value + "%");
                break;

            case LIKE_BACKWARD:
                // 後方一致
                parameterMap.put(key, "%" + value);
                break;

            case LIKE_PARTIAL:
                // 部分一致
                parameterMap.put(key, "%" + value + "%");
                break;

            default:
                break;
            }
        }
    }

    /**
     * SELECT COUNT句を作成する.
     * @param sql sql
     */
    private void generateSelectCountPhrase(final StringBuilder sql) {
        sql.append("SELECT COUNT(i.id)");
    }

    /**
     * 条件分割処理.
     * 入力値は、全半角スペースで分割する.
     * 分割した結果上限値を超える場合はエラーをthrowする.
     * @param conditions 分割前のテキスト
     * @return スペースで分割した検索条件
     *
     */
    private List<String> getSplitConditions(final String conditions) {

        final List<String> conditionsList = jp.co.jun.edi.util.StringUtils.splitWhitespace(conditions);

        if (isConditionsOverLimit(conditionsList)) {
            // 項目上限値エラー
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_003));
        }

        return conditionsList;

    }

    /**
     * 検索パラーメータが上限値以上設定されていないかチェックする.
     * @param conditions 条件のリスト
     * @return true：上限値を超えている
     *          falase：条件を超えていない
     */
    private boolean isConditionsOverLimit(final List<String> conditions) {

        if (conditions != null && conditions.size() > keywordConditionsLimitSize) {
            return true;
        }
        return false;
    }

    /**
     * クエリにパラメータを設定する.
     * @param parameterMap 検索条件
     * @param query クエリ
     */
    private void setQueryParameters(
            final Query query,
            final Map<String, Object> parameterMap) {
        parameterMap.forEach((key, value) -> {
            query.setParameter(key, value);
        });
    }

    /**
     * 検索条件で絞り込んだ件数を取得する.
     * @param sql sql
     * @param parameterMap パラメーターマップ
     * @return 件数
     */
    private long count(final String sql, final Map<String, Object> parameterMap) {
        final Query query = entityManager.createNativeQuery(sql);

        setQueryParameters(query, parameterMap);

        return ((Number) query.getSingleResult()).longValue();
    }
}
