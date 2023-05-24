package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ON・OFF型の分類.
 */
public enum OnOffType {
    /** OFF. */
    OFF(0),
    /** ON. */
    ON(1),
    /** 未選択. */
    NO_SELECT(-1);

    /**
     * value.
     */
    private final Integer value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    OnOffType(final Integer value) {
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
    public static Optional<OnOffType> findByValue(final int value) {
        return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、NO_SELECT を返却する.
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final OnOffType type) {
        if (Objects.isNull(type)) {
            return NO_SELECT.getValue();
        }

        return type.getValue();
    }

    /**
     * value を type に変換する.
     *
     * @param value value
     * @return type
     */
    public static OnOffType convertToType(final Integer value) {
        if (Objects.isNull(value)) {
            return NO_SELECT;
        }

        return findByValue(value).orElse(NO_SELECT);
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
    public static OnOffType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return NO_SELECT;
        }

        final Integer valInt = Integer.parseInt(value);
        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(value));
    }
}
