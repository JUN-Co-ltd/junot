package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * フクキタルデリバリ種別の定義.
 */
public enum FukukitaruMasterDeliveryType {
    /** 国内. */
    DOMESTIC(1),
    /** 海外. */
    OVERSEES(2);

    /**
     * value.
     */
    private final Integer value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    FukukitaruMasterDeliveryType(final Integer value) {
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
    public static Optional<FukukitaruMasterDeliveryType> findByValue(final Integer value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final FukukitaruMasterDeliveryType type) {
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
    public static FukukitaruMasterDeliveryType convertToType(final Integer value) {
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
    public static FukukitaruMasterDeliveryType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        Integer valInt = Integer.parseInt(value);

        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
