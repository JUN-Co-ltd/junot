package jp.co.jun.edi.repository.master.impl;

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

import jp.co.jun.edi.entity.master.UserEntity;
import jp.co.jun.edi.model.maint.MaintUserSearchConditionModel;
import jp.co.jun.edi.repository.master.custom.UserRepositoryCustom;
import jp.co.jun.edi.type.AuthorityType;
import jp.co.jun.edi.type.SearchMethodType;
import jp.co.jun.edi.type.SqlQueryCriteriaType;
import jp.co.jun.edi.type.SqlQuerySpecificationType;
import lombok.extern.slf4j.Slf4j;

/**
 * ユーザ情報Repositoryの実装クラス.
 */
@Slf4j
public class UserRepositoryImpl implements UserRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * ユーザマスタと仕入先マスタをJOINして、ユーザ情報を取得する.
     *
     * <pre>
     * {@code
     * SELECT
     *   u.id AS id
     *   , u.account_name AS account_name
     *   , u.enabled AS enabled
     *   , u.authority AS authority
     *   , u.company AS company
     *   , u.name AS name
     *   , u.mail_address AS mail_address
     *   , sm.sire AS maker_code
     *   , sm.name AS maker_name
     * FROM
     *   m_user u
     *   LEFT OUTER JOIN m_sirmst sm
     *     ON sm.sire = u.company
     *     AND sm.deleted_at IS NULL
     *     AND sm.mntflg IN ('1', '2', '')
     * WHERE
     *   u.deleted_at IS NULL
     *   AND u.system_managed = :systemManaged
     *   AND u.enabled IN (:enabledList)
     *   AND (
     *     -- 指定された権限を以下のパターンで設定
     *     u.authority = :authority                      --  完全一致
     *     OR u.authority LIKE :authority + ',%'         --  前方一致
     *     OR u.authority LIKE '%,' + :authority + ',%'  --  部分一致
     *     OR u.authority LIKE '%,' + :authority         --  後方一致
     *   )
     *   AND (
     *     -- すべてAND条件 完全一致の場合
     *     u.account_name = :accountName
     *     AND u.company = :company
     *     AND u.name = :name
     *     AND u.mail_address = :mail_address
     *     AND sm.sire = :makerCode
     *     AND sm.name = :makerName
     *   )
     *   AND (
     *     -- すべてOR条件 部分一致の場合
     *     u.account_name LIKE :accountName
     *     OR u.company LIKE :company
     *     OR u.name LIKE :name
     *     OR u.mail_address LIKE :mail_address
     *     OR sm.sire LIKE :makerCode
     *     OR sm.name LIKE :makerName
     *   )
     * ORDER BY
     *   u.company ASC
     *   , u.account_name ASC
     * }
     * </pre>
     */
    @Override
    public Page<UserEntity> findBySearchCondition(
            final MaintUserSearchConditionModel searchCondition,
            final Pageable pageable) {
        // SELECT COUNT句を生成
        final StringBuilder sqlCount = new StringBuilder();
        generateSelectCountPhrase(sqlCount);

        // FROM句を生成
        final StringBuilder sqlFrom = new StringBuilder();
        generateFromPhrase(sqlFrom);

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

        if (log.isDebugEnabled()) {
            log.debug("sql:" + sql.toString());
        }

        final Query query = entityManager.createNativeQuery(sql.toString(), UserEntity.class);

        // クエリにパラメータを設定
        setQueryParameters(query, parameterMap);

        // 開始位置を設定
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

        // 取得件数を設定
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        final List<UserEntity> list = query.getResultList();

        return new PageImpl<>(list, pageable, count);
    }

    /**
     * SELECT句を作成する.
     * @param sql sql
     */
    private void generateSelectPhrase(final StringBuilder sql) {
        final List<String> sqlCalamus = new ArrayList<>();

        sqlCalamus.add("u.id AS id");
        sqlCalamus.add("u.account_name AS account_name");
        sqlCalamus.add("u.enabled AS enabled");
        sqlCalamus.add("u.authority AS authority");
        sqlCalamus.add("u.company AS company");
        sqlCalamus.add("u.name AS name");
        sqlCalamus.add("u.mail_address AS mail_address");
        sqlCalamus.add("sm.sire AS maker_code");
        sqlCalamus.add("sm.name AS maker_name");

        sql.append("SELECT ").append(StringUtils.join(sqlCalamus, ", "));
    }

    /**
     * SELECT COUNT句を作成する.
     * @param sql sql
     */
    private void generateSelectCountPhrase(final StringBuilder sql) {
        sql.append("SELECT COUNT(u.id)");
    }

    /**
     * FROM句を作成する.
     * @param sql sql
     */
    private void generateFromPhrase(final StringBuilder sql) {
        sql.append(" FROM");
        sql.append(" m_user u");
        sql.append(" LEFT OUTER JOIN m_sirmst sm");
        sql.append(" ON sm.sire = u.company");
        sql.append(" AND sm.deleted_at IS NULL");
        sql.append(" AND sm.mntflg IN ('1', '2', '')");
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
            final MaintUserSearchConditionModel searchCondition) {
        final List<String> sqlCalamus = new ArrayList<>();

        sqlCalamus.add("u.deleted_at IS NULL");

        addSqlWhereBoolean(sqlCalamus, parameterMap, "systemManaged", "u.system_managed", SqlQueryCriteriaType.EQUAL, searchCondition.isSystemManaged());
        addSqlWhereBooleanList(sqlCalamus, parameterMap, "enabledList", "u.enabled", searchCondition.getEnabledList());
        addSqlWhereAuthorities(sqlCalamus, parameterMap, "authority", "u.authority", searchCondition.getAuthorities());

        if (searchCondition.getSearchMethod() == SearchMethodType.ALL_AND_FULL) {
            // すべてAND条件 完全一致の場合
            addSqlWhereSearchCondition(sqlCalamus, parameterMap, SqlQuerySpecificationType.AND, SqlQueryCriteriaType.EQUAL, searchCondition);
        } else if (searchCondition.getSearchMethod() == SearchMethodType.ALL_OR_LIKE) {
            // すべてOR条件 部分一致の場合
            addSqlWhereSearchCondition(sqlCalamus, parameterMap, SqlQuerySpecificationType.OR, SqlQueryCriteriaType.LIKE_PARTIAL, searchCondition);
        }

        if (!sqlCalamus.isEmpty()) {
            sql.append(" WHERE ").append(StringUtils.join(sqlCalamus, " " + SqlQuerySpecificationType.AND + " "));
        }
    }

    /**
     * SQLにWHERE句のカラムを追加する.
     * @param sqlCalamus SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param specification 論理演算子（OR、AND）
     * @param criteria 比較演算子（"=", "LIKE"）
     * @param searchCondition 検索条件
     */
    private void addSqlWhereSearchCondition(
            final List<String> sqlCalamus,
            final Map<String, Object> parameterMap,
            final SqlQuerySpecificationType specification,
            final SqlQueryCriteriaType criteria,
            final MaintUserSearchConditionModel searchCondition) {
        final List<String> sqlSubCalamus = new ArrayList<>();

        addSqlWhereStringList(sqlSubCalamus, parameterMap, "accountName", "u.account_name", criteria, searchCondition.getAccountNames());
        addSqlWhereStringList(sqlSubCalamus, parameterMap, "company", "u.company", criteria, searchCondition.getCompanies());
        addSqlWhereStringList(sqlSubCalamus, parameterMap, "name", "u.name", criteria, searchCondition.getNames());
        addSqlWhereStringList(sqlSubCalamus, parameterMap, "mailAddress", "u.mail_address", criteria, searchCondition.getMailAddresses());
        addSqlWhereStringList(sqlSubCalamus, parameterMap, "makerCode", "sm.sire", criteria, searchCondition.getMakerCodes());
        addSqlWhereStringList(sqlSubCalamus, parameterMap, "makerName", "sm.name", criteria, searchCondition.getMakerCodes());

        if (!sqlSubCalamus.isEmpty()) {
            sqlCalamus.add("(" + StringUtils.join(sqlSubCalamus, " " + specification.getValue() + " ") + ")");
        }
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
        if (value != null) {
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
     * SQLにWHERE句のカラムを追加する.
     * @param sqlCalamus SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param keyPrefix キーのプレフィックス
     * @param columnName カラム名
     * @param criteria 比較演算子（"=", "LIKE"）
     * @param values 値のリスト
     */
    private void addSqlWhereStringList(
            final List<String> sqlCalamus,
            final Map<String, Object> parameterMap,
            final String keyPrefix,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final List<String> values) {
        if (values != null) {
            int i = 0;

            for (final String value : values) {
                addSqlWhereString(sqlCalamus, parameterMap, keyPrefix + i++, columnName, criteria, value);
            }
        }
    }

    /**
     * SQLにWHERE句のカラムを追加する.
     * @param sqlCalamus SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param criteria 比較演算子（"="）
     * @param value 値
     */
    private void addSqlWhereBoolean(
            final List<String> sqlCalamus,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final Boolean value) {
        if (value != null) {
            sqlCalamus.add(columnName + " " + criteria.getValue() + " :" + key);
            parameterMap.put(key, value);
        }
    }

    /**
     * SQLにWHERE句のカラムを追加する.
     * @param sqlCalamus SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param values 値のリスト
     */
    private void addSqlWhereBooleanList(
            final List<String> sqlCalamus,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final List<Boolean> values) {
        if (values != null) {
            sqlCalamus.add(columnName + " IN (:" + key + ")");
            parameterMap.put(key, values);
        }
    }

    /**
     * SQLにWHERE句のカラムを追加する.
     * @param sqlCalamus SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param keyPrefix キーのプレフィックス
     * @param columnName カラム名
     * @param values 値のリスト
     */
    private void addSqlWhereAuthorities(
            final List<String> sqlCalamus,
            final Map<String, Object> parameterMap,
            final String keyPrefix,
            final String columnName,
            final List<AuthorityType> values) {
        if (values != null) {
            final List<String> sqlSubCalamus = new ArrayList<>();

            int i = 0;

            for (final AuthorityType value : values) {
                addSqlWhereString(sqlSubCalamus, parameterMap, keyPrefix + i++, columnName, SqlQueryCriteriaType.EQUAL, value.getValue());
                addSqlWhereString(sqlSubCalamus, parameterMap, keyPrefix + i++, columnName, SqlQueryCriteriaType.LIKE_FORWARD, value.getValue() + ",");
                addSqlWhereString(sqlSubCalamus, parameterMap, keyPrefix + i++, columnName, SqlQueryCriteriaType.LIKE_PARTIAL, "," + value.getValue() + ",");
                addSqlWhereString(sqlSubCalamus, parameterMap, keyPrefix + i++, columnName, SqlQueryCriteriaType.LIKE_BACKWARD, "," + value.getValue());
            }

            if (!sqlSubCalamus.isEmpty()) {
                sqlCalamus.add("(" + StringUtils.join(sqlSubCalamus, " " + SqlQuerySpecificationType.OR.getValue() + " ") + ")");
            }
        }
    }

    /**
     * ORDER BY句を作成する.
     * @param sql sql
     */
    private void generateOrderByPhrase(final StringBuilder sql) {
        sql.append(" ORDER BY");
        sql.append(" u.company ASC");
        sql.append(" , u.account_name ASC");
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
