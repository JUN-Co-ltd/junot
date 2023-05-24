package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 生産ステータスの分類.
 * 他費目でも共通になる可能性があるので、別名にしておく
 */
public enum ProductionStatusType {
    /** サンプル(0). */
    SAMPLE(0),
    /** 品質表示(1). */
    QUALITY_DISPLAY(1),
    /** 下げ札出荷(2). */
    TAG_SHIPMENT(2),
    /** 仕様確定(3). */
    SPECIFICATION_FIX(3),
    /** 生地出荷(4). */
    TEXTURE_SHIPMENT(4),
    /** 生地入荷(5). */
    TEXTURE_ARRIVAL(5),
    /** 付属入荷(6). */
    ATTACHMENT_ARRIVAL(6),
    /** 縫製中(7). */
    SEWING_IN(7),
    /** 縫製検品(8). */
    SEW_INSPECTION(8),
    /** 検品(9). */
    INSPECTION(9),
    /** SHIP(10). */
    SHIP(10),
    /** DISTA入荷日(11). */
    DISTA_ARRIVAL(11),
    /** DISTA仕入日(12). */
    DISTA_PURCHASE(12);

    /**
     * value.
     */
    private final int value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    ProductionStatusType(final int value) {
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
    public static Optional<ProductionStatusType> findByValue(final int value) {
        return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final ProductionStatusType type) {
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
    public static ProductionStatusType convertToType(final Integer value) {
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
    public static ProductionStatusType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        Integer valInt = Integer.parseInt(value);
        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(value));
    }
}
