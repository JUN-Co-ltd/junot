package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 伝票種別の定義.
 */
public enum CheckType {
    /** 売上. */
    SALES(0),
    /** 返品. */
    RETURN(1),
    /** 小口入金. */
    SMALL_DEPOSIT(2),
    /** 小口出金. */
    SMALL_WITHDRAWAL(3),
    /** 現金回収. */
    CASH_RECOVERY(4),
    /** 両替. */
    EXCHANGE(5),
    /** 取置受付. */
    RESERVE_RECEPTION(6),
    /** 取置取消. */
    RESEREVE_CANCEL(7),
    /** 売掛回収. */
    ACCOUNTS_RECEIVABLE_RECOVERY(8),
    /** CF取消. */
    CASH_FLOW_CANCEL(9);

    /**
     * value.
     */
    private final Integer value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    CheckType(final Integer value) {
        this.value = value;
    }

    /**
     * valueを取得する.
     *
     * @return value
     */
    public Integer getValue() {
        return value;
    }

    /**
     * valueからtypeを検索する.
     *
     * @param value value
     * @return Optional<EnabledType>
     */
    public static Optional<CheckType> findByValue(final Integer value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final CheckType type) {
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
    public static CheckType convertToType(final Integer value) {
        if (Objects.isNull(value)) {
            return null;
        }

        return findByValue(value).orElse(null);
    }

    /**
     * [JSON変換用] valueを取得する.
     *
     * @return Integer
     */
    @JsonValue
    public Integer convertToValue() {
        return getValue();
    }

    /**
     * [JSON変換用] typeを取得する.
     *
     * @param value value
     * @return ResultType
     */
    @JsonCreator
    public static CheckType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        final Integer valInt = Integer.parseInt(value);

        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
