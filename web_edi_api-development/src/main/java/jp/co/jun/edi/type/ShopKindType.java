package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 店舗区分の定義.
 */
public enum ShopKindType {
    /** 店舗. */
    SHOP(0),
    /** 本部. */
    HEAD(1),
    /** 倉庫. */
    WARE_HOUSE(2);

    /**
     * value.
     */
    private final Integer value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    ShopKindType(final Integer value) {
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
    public static Optional<ShopKindType> findByValue(final Integer value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final ShopKindType type) {
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
    public static ShopKindType convertToType(final Integer value) {
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
    public static ShopKindType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        final Integer valInt = Integer.parseInt(value);

        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
