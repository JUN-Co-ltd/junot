package jp.co.jun.edi.repository.master.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.master.SireEntity;
import jp.co.jun.edi.model.maint.MaintSireSearchConditionModel;
import jp.co.jun.edi.repository.master.custom.SireRepositoryCustom;
import jp.co.jun.edi.type.SqlQueryCriteriaType;
import jp.co.jun.edi.type.SqlQuerySpecificationType;
import jp.co.jun.edi.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 取引先情報Repository実装クラス.
 */
@Slf4j
public class SireRepositoryImpl implements SireRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<SireEntity> findBySearchCondition(
            final MaintSireSearchConditionModel searchCondition,
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

        generateOrderByPhrase(sql);

        if (log.isDebugEnabled()) {
            log.debug("sql:" + sql.toString());
        }

        final Query query = entityManager.createNativeQuery(sql.toString(), SireEntity.class);

        // クエリにパラメータを設定
        QueryUtils.setQueryParameters(query, parameterMap);

        // 開始位置を設定
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        // 取得件数を設定
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        final List<SireEntity> result = query.getResultList();

        return new PageImpl<>(result, pageable, count);

    }

    /**
     * @param sqlWhere WHERE句
     * @param parameterMap パラメータ
     * @param searchCondition 検索条件
     * @return レコード件数
     */
    private long countAllRecord(final StringBuilder sqlWhere, final Map<String, Object> parameterMap,
            final MaintSireSearchConditionModel searchCondition) {
        final StringBuilder sqlCount = new StringBuilder();
        final StringBuilder sqlFrom = new StringBuilder();

        sqlCount.append(" SELECT COUNT(rslt.id)");
        sqlCount.append(" FROM (SELECT k.id AS id");

        generateFromPhrase(sqlFrom, searchCondition, parameterMap);
        sqlCount.append(sqlFrom).append(sqlWhere);

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

        sqlColumns.add("k.id AS id");
        sqlColumns.add("k.reckbn AS reckbn");
        sqlColumns.add("k.sire AS sire_code");
        sqlColumns.add("sm.name AS sire_name");
        sqlColumns.add("k.kojcd AS koj_code");
        sqlColumns.add("k.name AS koj_name");
        sqlColumns.add("k.hkiji AS hkiji");
        sqlColumns.add("k.hseihin AS hseihin");
        sqlColumns.add("k.hnefuda AS hnefuda");
        sqlColumns.add("k.hfuzoku AS hfuzoku");
        sqlColumns.add("k.brand1 AS brand_code");
        sqlColumns.add("k.hsofkbn AS hsofkbn");
        sqlColumns.add("k.nsofkbn AS nsofkbn");
        sqlColumns.add("k.ysofkbn AS ysofkbn");
        sqlColumns.add("sm.yugaikbn AS yugaikbn");

        sql.append("SELECT ").append(StringUtils.join(sqlColumns, ", "));
    }

    /**
     * FROM句を生成.
     * @param sql sql
     * @param searchCondition 検索条件
     * @param parameterMap パラメーターマップ
     */
    private void generateFromPhrase(final StringBuilder sql,
            final MaintSireSearchConditionModel searchCondition,
            final Map<String, Object> parameterMap) {

        sql.append(" FROM m_kojmst k");

        sql.append("    LEFT OUTER JOIN m_sirmst sm");
        sql.append("         ON k.sire = sm.sire");
        sql.append("         AND sm.deleted_at IS NULL");
        sql.append("         AND sm.mntflg IN ('1', '2', '')");

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
            final MaintSireSearchConditionModel searchCondition) {
        final List<String> sqlColumns = new ArrayList<>();
        final List<String> sqlBrandColumns = new ArrayList<>();

        sqlColumns.add("k.deleted_at IS NULL");
        sqlColumns.add("k.mntflg IN ('1', '2', '')");

        /** 仕入先. */
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "sire", "k.sire",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getSireCode(), null));
        /** 区分. */
        QueryUtils.addSqlWhere(sqlColumns, parameterMap, "reckbn", "k.reckbn", searchCondition.getReckbns());
        if (searchCondition.isUnusedCodeFlg()) {
          // 未使用コードにチェックがついている場合
          QueryUtils.addSqlWhere(sqlColumns, parameterMap, "name", "k.name",
                  SqlQueryCriteriaType.LIKE_FORWARD, "Ａ%");
        } else {
          // 未使用コードにチェックがついていない場合
          QueryUtils.addSqlWhere(sqlColumns, parameterMap, "name", "k.name",
                  SqlQueryCriteriaType.NOT_LIKE, "Ａ%");
        }

        /** ブランド1. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand1", "k.brand1",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド2. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand2", "k.brand2",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド3. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand3", "k.brand3",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド4. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand4", "k.brand4",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド5. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand5", "k.brand5",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド6. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand6", "k.brand6",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド7. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand7", "k.brand7",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド8. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand8", "k.brand8",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド9. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand9", "k.brand9",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド10. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand10", "k.brand10",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド11. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand11", "k.brand11",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド12. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand12", "k.brand12",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド13. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand13", "k.brand13",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド14. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand14", "k.brand14",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド15. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand15", "k.brand15",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド16. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand16", "k.brand16",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド17. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand17", "k.brand17",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド18. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand18", "k.brand18",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド19. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand19", "k.brand19",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド20. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand20", "k.brand20",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド21. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand21", "k.brand21",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド22. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand22", "k.brand22",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド23. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand23", "k.brand23",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド24. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand24", "k.brand24",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド25. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand25", "k.brand25",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド26. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand26", "k.brand26",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド27. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand27", "k.brand27",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド28. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand28", "k.brand28",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド29. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand29", "k.brand29",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));
        /** ブランド30. */
        QueryUtils.addSqlWhere(sqlBrandColumns, parameterMap, "brand30", "k.brand30",
                SqlQueryCriteriaType.EQUAL, StringUtils.defaultIfEmpty(searchCondition.getBrandCode(), null));

        sql.append(" WHERE ").append(StringUtils.join(sqlColumns, " " + SqlQuerySpecificationType.AND + " "));
        if (!CollectionUtils.isEmpty(sqlBrandColumns)) {
        	sql.append(" AND ( ").append(StringUtils.join(sqlBrandColumns, " " + SqlQuerySpecificationType.OR + " ")).append(" ) ");
        }
    }

    /**
     * ORDER BY句を生成.
     * @param sql sql
     */
    private void generateOrderByPhrase(final StringBuilder sql) {
        sql.append(" ORDER BY ");
        sql.append(" k.sire ");
    }
}
