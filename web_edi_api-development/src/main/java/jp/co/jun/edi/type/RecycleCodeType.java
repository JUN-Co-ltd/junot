package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 連携ステータスの定義.
 */
public enum RecycleCodeType {
    /** 0 : 連携対象. */
    TARGET("0"),
    /** 1 : 連携中. */
    LINKING("1"),
    /** 2 : 連携済. */
    ALREADY("2"),
    /** 9 : 連携対象外. */
    EXCLUDED("9"),;

    /**
     * value.
     */
    private final String value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    RecycleCodeType(final String value) {
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
    public static Optional<RecycleCodeType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static String convertToValue(final RecycleCodeType type) {
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
    public static RecycleCodeType convertToType(final String value) {
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
    public static RecycleCodeType convertToJson(final String value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
