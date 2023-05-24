package jp.co.jun.edi.type;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 仕入区分の定義.
 */
public enum PurchaseType {
    /** 追加仕入. */
    ADDITIONAL_PURCHASE(1),
    /** 追加仕入赤. */
    ADDITIONAL_PURCHASE_RED(2),
    /** 仕入返品. */
    RETURN_PURCHASE(3),
    /** 仕入返品赤. */
    RETURN_PURCHASE_RED(4),
    /** 配分出荷仕入(一括). */
    SHIPMENT_PURCHASE_LUMP(5),
    /** 配分出荷仕入(課別). */
    SHIPMENT_PURCHASE_DIVISION(6),
    /** 直送仕入. */
    DIRECT_PURCHASE(7),
    /** 店舗発注分仕入. */
    STORE_PURCHASE(9);

    /**
     * value.
     */
    private final int value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    PurchaseType(final int value) {
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
    public static Optional<PurchaseType> findByValue(final int value) {
        return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final PurchaseType type) {
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
    public static PurchaseType convertToType(final Integer value) {
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
    public static PurchaseType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        final Integer valInt = Integer.parseInt(value);
        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(value));
    }
}
