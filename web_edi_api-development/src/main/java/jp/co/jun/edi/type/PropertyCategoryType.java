package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * プロパティマスタのカテゴリの定義.
 */
public enum PropertyCategoryType {
    /** 1:共通（システム全体で共通的に利用）. */
    COMMON(1),
    /** 2:ASTERIA（ASTERIAのみで利用）. */
    ASTERIA(2),
    /** 3:バッチ（Javaバッチのみで利用）. */
    BATCH(3);

    /**
     * value.
     */
    private final int value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    PropertyCategoryType(final int value) {
        this.value = value;
    }

    /**
     * valueを取得する.
     *
     * @return value
     */
    public int getValue() {
        return value;
    }

    /**
     * valueからtypeを検索する.
     *
     * @param value value
     * @return Optional<EnabledType>
     */
    public static Optional<PropertyCategoryType> findByValue(final int value) {
        return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final PropertyCategoryType type) {
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
    public static PropertyCategoryType convertToType(final Integer value) {
        if (Objects.isNull(value)) {
            return null;
        }

        return findByValue(value).orElse(null);
    }

    /**
     * [JSON変換用] valueを取得する.
     *
     * @return int
     */
    @JsonValue
    public int convertToValue() {
        return getValue();
    }

    /**
     * [JSON変換用] typeを取得する.
     *
     * @param value value
     * @return ResultType
     */
    @JsonCreator
    public static PropertyCategoryType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        Integer valInt = Integer.parseInt(value);
        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(value));
    }
}
