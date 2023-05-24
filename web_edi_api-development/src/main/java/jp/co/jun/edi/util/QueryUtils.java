package jp.co.jun.edi.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import jp.co.jun.edi.entity.PurchaseRecordSumCompositeEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.OnOffType;
import jp.co.jun.edi.type.SqlQueryCriteriaType;
import jp.co.jun.edi.type.SqlQuerySpecificationType;

/**
 * クエリユーティリティ.
 */
public final class QueryUtils {
    /** WHERE句 AND結合用. */
    private static final String WHERE_AMD = " " + SqlQuerySpecificationType.AND + " ";

    /** WHERE句 OR結合用. */
    private static final String WHERE_OR = " " + SqlQuerySpecificationType.OR + " ";

    /** BigInteger型の-1のリスト. */
    private static final List<BigInteger> BIGINTEGER_MINUS_1_LIST = Arrays.asList(BigInteger.ONE.negate());

    /**
     */
    private QueryUtils() {
    }

    /**
     * SQLにWHERE句のカラムを追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param criteria 比較演算子
     * @param value 値
     */
    public static void addSqlWhere(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final Object value
            ) {
        if (value != null) {
            sqlColumns.add(columnName + " " + criteria.getValue() + " :" + key);
            parameterMap.put(key, value);
        }
    }

    /**
     * SQLにWHERE句のカラム(List<String>)を追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param value 値(リスト)
     */
    public static void addSqlWhere(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final List<String> value
            ) {
        if (value != null && !value.isEmpty()) {
            sqlColumns.add(columnName + " " + SqlQueryCriteriaType.IN + " (:" + key + ")");
            parameterMap.put(key, value);
        }
    }

    /**
     * SQLにWHERE句のカラム(BooleanType)を追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param criteria 比較演算子
     * @param value 値
     */
    public static void addSqlWhere(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final BooleanType value
            ) {
        if (value != null) {
            sqlColumns.add(columnName + " " + criteria.getValue() + " :" + key);
            parameterMap.put(key, value.getValue());
        }
    }

    /**
     * SQLにWHERE句のカラム(OnOffType)を追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param criteria 比較演算子
     * @param value 値
     */
    public static void addSqlWhere(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final OnOffType value
            ) {
        if (value != null && OnOffType.NO_SELECT != value) {
            sqlColumns.add(columnName + " " + criteria.getValue() + " :" + key);
            parameterMap.put(key, value.getValue());
        }
    }

    /**
     * SQLにWHERE句のカラム(Date)を追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param criteria 比較演算子
     * @param value 値
     */
    public static void addSqlWhere(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final Date value
            ) {
        if (value != null) {
            sqlColumns.add(columnName + " " + criteria.getValue() + " :" + key);
            parameterMap.put(key, DateUtils.truncateDate(value));
        }
    }

    /**
     * SQLにWHERE句のカラムを追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param criteria 比較演算子
     * @param value 値
     */
    public static void addSqlWhere(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final Integer value) {
        if (value != null) {
            sqlColumns.add(columnName + " " + criteria.getValue() + " :" + key);
            parameterMap.put(key, value);
        }
    }

    /**
     * SQLにWHERE句のカラムを追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param criteria 比較演算子
     * @param value 値
     */
    public static void addSqlWhere(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final BigInteger value) {
        if (value != null) {
            sqlColumns.add(columnName + " " + criteria.getValue() + " :" + key);
            parameterMap.put(key, value);
        }
    }

    /**
     * SQLにWHERE句のカラム(Integerへキャスト)を追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param criteria 比較演算子
     * @param value 値
     */
    public static void addSqlWhereCastInteger(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final String value
            ) {
        if (value != null) {
            sqlColumns.add(columnName + " " + criteria.getValue() + " CAST(:" + key + " AS UNSIGNED)");
            parameterMap.put(key, value);
        }
    }

    /**
     * SQLにWHERE句のカラムを追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param criteria 比較演算子（"=", "LIKE"）
     * @param value 値のリスト
     */
    public static void addSqlWhereString(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final String value) {
        if (value != null) {
            sqlColumns.add(columnName + " " + criteria.getValue() + " :" + key);

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
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param keyPrefix キーのプレフィックス
     * @param columnName カラム名
     * @param criteria 比較演算子（"=", "LIKE"）
     * @param values 値のリスト
     */
    public static void addSqlWhereStringList(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String keyPrefix,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final List<String> values) {
        if (CollectionUtils.isNotEmpty(values)) {
            int i = 0;

            for (final String value : values) {
                addSqlWhereString(sqlColumns, parameterMap, keyPrefix + i++, columnName, criteria, value);
            }
        }
    }

    /**
     * SQLにWHERE句のカラムを追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param criteria 比較演算子（"="）
     * @param value 値
     */
    public static void addSqlWhereBoolean(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final SqlQueryCriteriaType criteria,
            final Boolean value) {
        if (value != null) {
            sqlColumns.add(columnName + " " + criteria.getValue() + " :" + key);
            parameterMap.put(key, value);
        }
    }

    /**
     * SQLにWHERE句のカラムを追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param values 値のリスト
     */
    public static void addSqlWhereBooleanList(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final List<Boolean> values) {
        if (CollectionUtils.isNotEmpty(values)) {
            sqlColumns.add(columnName + " IN (:" + key + ")");
            parameterMap.put(key, values);
        }
    }

    /**
     * SQLにWHERE句のカラムをINで追加する.
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param values 値のリスト
     */
    public static void addSqlWhereIn(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final List<?> values) {
        if (CollectionUtils.isNotEmpty(values)) {
            sqlColumns.add(columnName + " IN (:" + key + ")");
            parameterMap.put(key, values);
        }
    }

    /**
     * SQLにWHERE句のカラム(Date)を追加する.
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @param parameterMap パラメーターマップ
     * @param key キー
     * @param columnName カラム名
     * @param fromDate 開始年月日
     * @param toDate 終了年月日
     */
    public static void addSqlWhereFromTo(
            final List<String> sqlColumns,
            final Map<String, Object> parameterMap,
            final String key,
            final String columnName,
            final Date fromDate,
            final Date toDate) {
        if (fromDate != null) {
            // 開始年月日がある場合、検索条件に追加する
            QueryUtils.addSqlWhere(sqlColumns, parameterMap, key + "From", columnName,
                    SqlQueryCriteriaType.GREATER_THAN_OR_EQUAL_TO, fromDate);
        }

        if (toDate != null) {
            // 終了年月日がある場合、検索条件に追加する
            QueryUtils.addSqlWhere(sqlColumns, parameterMap, key + "To", columnName,
                    SqlQueryCriteriaType.LESS_THAN_OR_EQUAL_TO, toDate);
        }
    }

    /**
     * クエリにパラメーターを設定.
     * @param parameterMap 検索条件
     * @param query クエリ
     */
    public static void setQueryParameters(final Query query, final Map<String, Object> parameterMap) {
        parameterMap.forEach((key, value) -> query.setParameter(key, value));
    }

    /**
     * 件数を取得(検索条件で絞り込んだ後).
     * @param query クエリ
     * @param parameterMap パラメーターマップ
     * @return 件数
     */
    public static long count(final Query query, final Map<String, Object> parameterMap) {
        QueryUtils.setQueryParameters(query, parameterMap);
        return ((Number) query.getSingleResult()).longValue();
    }

    //PRD_0133 #10181 add JFE start
    /**
     * 件数を取得(検索条件で絞り込んだ後).
     * @param query クエリ
     * @param parameterMap パラメーターマップ
     * @return
     * @return 件数
     */
	@SuppressWarnings("unchecked")
	public static  List<PurchaseRecordSumCompositeEntity> list(final Query query, final Map<String, Object> parameterMap) {
        QueryUtils.setQueryParameters(query, parameterMap);
        return   query.getResultList();
    }
    //PRD_0133 #10181 add JFE end

    /**
     * WHERE句のカラムのリストをANDで結合する.
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @return 結合したSQL
     */
    public static String toAndJoin(final List<String> sqlColumns) {
        return "(" + StringUtils.join(sqlColumns, WHERE_AMD) + ")";
    }

    /**
     * WHERE句のカラムのリストをORで結合する.
     *
     * @param sqlColumns SQLのWHERE句のカラムのリスト
     * @return 結合したSQL
     */
    public static String toOrJoin(final List<String> sqlColumns) {
        return "(" + StringUtils.join(sqlColumns, WHERE_OR) + ")";
    }

    /**
     * 条件分割処理.
     *
     * <pre>
     * 入力値は、全半角スペースで分割する。
     * 分割した結果上限値を超える場合はエラーをthrowする。
     * </pre>
     *
     * @param conditions 分割前のテキスト
     * @param keywordConditionsLimitSize 分割可能な上限値
     * @return 分割した検索条件
     * @throws BusinessException 上限値を超えている
     */
    public static List<String> getSplitConditions(final String conditions, final int keywordConditionsLimitSize) {
        final List<String> values = jp.co.jun.edi.util.StringUtils.splitWhitespace(conditions);

        // 項目上限値チェック
        validateConditionsOverLimit(values, keywordConditionsLimitSize);

        return values;
    }

    /**
     * 発注Noの条件分割処理.
     *
     * <pre>
     * 入力値は、全半角スペースで分割する。
     * 分割した結果上限値を超える場合はエラーをthrowする。
     * 0（受注状態）やマイナス値、nullや文字列の場合に-1のリストを返却する（検索対象から除外するため）。
     * </pre>
     *
     * @param conditions 分割前のテキスト
     * @param keywordConditionsLimitSize 分割可能な上限値
     * @return 分割した検索条件
     * @throws BusinessException 上限値を超えている
     */
    public static List<BigInteger> getSplitConditionsOrderNumber(final String conditions, final int keywordConditionsLimitSize) {
        final List<String> values = jp.co.jun.edi.util.StringUtils.splitWhitespace(conditions);

        if (CollectionUtils.isEmpty(values)) {
            // 検索条件が存在しない場合、空を返却する
            return Collections.emptyList();
        }

        // 項目上限値チェック
        validateConditionsOverLimit(values, keywordConditionsLimitSize);

        // 配列の中をBigIntegerに変換
        final List<BigInteger> bigIntegers = values.stream()
                // BigIntegerに変換する（nullや文字列の場合に0が返却される）
                .map(value -> jp.co.jun.edi.util.NumberUtils.createBigInteger(value))
                // value > 0 の場合、リストに追加する
                .filter(value -> value.compareTo(BigInteger.ZERO) == 1)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(bigIntegers)) {
            // 検索条件が存在しない場合、-1のリストを返却する
            return BIGINTEGER_MINUS_1_LIST;
        }

        return bigIntegers;
    }

    /**
     * 値が上限値以上設定されていないかチェックする.
     *
     * @param conditions 値のリスト
     * @param keywordConditionsLimitSize 分割可能な上限値
     * @throws BusinessException 上限値を超えている
     */
    public static void validateConditionsOverLimit(final List<String> conditions, final int keywordConditionsLimitSize) {
        if (CollectionUtils.size(conditions) > keywordConditionsLimitSize) {
            // 項目上限値エラー
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_003));
        }
    }
}
