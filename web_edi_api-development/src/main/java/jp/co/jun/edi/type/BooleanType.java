package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Boolean型の分類.
 * NULL値は「FALSE」と同義とする
 */
public enum BooleanType {
    /** 無効(false). */
    FALSE(Boolean.FALSE),
    /** 有効(true). */
    TRUE(Boolean.TRUE);


    /**
     * value.
     */
    private final boolean value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    BooleanType(final boolean value) {
        this.value = value;
    }

    /**
     * valueを取得する.
     *
     * @return value
     */
    public boolean getValue() {
        return value;
    }

    /**
     * valueからtypeを検索する.
     *
     * @param value value
     * @return Optional<EnabledType>
     */
    public static Optional<BooleanType> findByValue(final boolean value) {
        return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static boolean convertToValue(final BooleanType type) {
        if (Objects.isNull(type)) {
            return false;
        }

        return type.getValue();
    }

    /**
     * value を type に変換する. value が null の場合、null を返却する.
     *
     * @param value value
     * @return type
     */
    public static BooleanType convertToType(final Boolean value) {
        if (Objects.isNull(value)) {
            return FALSE;
        }

        return findByValue(value).orElse(FALSE);
    }

    /**
     * [JSON変換用] valueを取得する.
     *
     * @return int
     */
    @JsonValue
    public boolean convertToValue() {
        return getValue();
    }

    /**
     * [JSON変換用] typeを取得する.
     *
     * @param value value
     * @return ResultType
     */
    @JsonCreator
    public static BooleanType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return FALSE;
        }

        final Boolean valInt = Boolean.parseBoolean(value);
        return findByValue(valInt.booleanValue()).orElseThrow(() -> new IllegalArgumentException(value));
    }
}
