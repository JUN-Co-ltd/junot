package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 発注書出力フラグの定義.
 */
public enum OrderSheetOutType {
    /** 未発行. */
    NOT_ISSUE(Boolean.FALSE),
    /** 発行済. */
    ISSUED(Boolean.TRUE);

    /**
     * value.
     */
    private final Boolean value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    OrderSheetOutType(final Boolean value) {
        this.value = value;
    }

    /**
     * valueを取得する.
     *
     * @return value
     */
    public Boolean getValue() {
        return value;
    }

    /**
     * valueからtypeを検索する.
     *
     * @param value value
     * @return Optional<EnabledType>
     */
    public static Optional<OrderSheetOutType> findByValue(final Boolean value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type type
     * @return value
     */
    public static Boolean convertToValue(final OrderSheetOutType type) {
        if (Objects.isNull(type)) {
            return Boolean.FALSE;
        }
        return type.getValue();
    }

    /**
     * value を type に変換する. value が null の場合、null を返却する.
     *
     * @param value value
     * @return type
     */
    public static OrderSheetOutType convertToType(final Boolean value) {
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
    public Boolean convertToValue() {
        return getValue();
    }

    /**
     * [JSON変換用] typeを取得する.
     *
     * @param value value
     * @return ResultType
     */
    @JsonCreator
    public static OrderSheetOutType convertToJson(final Boolean value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
