package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * SQLのクエリの比較演算子の定義.
 */
public enum SqlQueryCriteriaType {
    /** = : 完全一致. */
    EQUAL("="),
    /** LIKE : 前方一致. */
    LIKE_FORWARD("LIKE"),
    /** LIKE : 後方一致. */
    LIKE_BACKWARD("LIKE"),
    /** LIKE : 部分一致. */
    LIKE_PARTIAL("LIKE"),
    /** 2022/12/07 */
    NOT_LIKE("NOT LIKE"),
    /** >= : 以上. */
    GREATER_THAN_OR_EQUAL_TO(">="),
    /** <= : 以下. */
    LESS_THAN_OR_EQUAL_TO("<="),
    /** >. */
    GREATER_THAN(">"),
    /** <. */
    LESS_THAN("<"),
    /** IN. */
    IN("IN");


    /**
     * value.
     */
    private final String value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    SqlQueryCriteriaType(final String value) {
        this.value = value;
    }

    /**
     * valueを取得する.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * valueからtypeを検索する.
     *
     * @param value value
     * @return Optional<EnabledType>
     */
    public static Optional<SqlQueryCriteriaType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type type
     * @return value
     */
    public static String convertToValue(final SqlQueryCriteriaType type) {
        if (Objects.isNull(type)) {
            return null;
        }
        return type.getValue();
    }

    /**
     * value を type に変換する. value が null の場合、null を返却する.
     *
     * @param value value
     * @return type
     */
    public static SqlQueryCriteriaType convertToType(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        return findByValue(value).orElse(null);
    }

    /**
     * [JSON変換用] valueを取得する.
     *
     * @return String
     */
    @JsonValue
    public String convertToValue() {
        return getValue();
    }

    /**
     * [JSON変換用] typeを取得する.
     *
     * @param value value
     * @return ResultType
     */
    @JsonCreator
    public static SqlQueryCriteriaType convertToJson(final String value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
