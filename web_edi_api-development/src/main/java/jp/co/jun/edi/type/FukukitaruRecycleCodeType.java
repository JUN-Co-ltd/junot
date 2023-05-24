package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * シールへのリサイクルマーク印字の定義.
 */
public enum FukukitaruRecycleCodeType {
    /** 紙[帯]・ﾌﾟﾗ[袋]. */
    PAPER_BAND_PLASTIC_BAG("紙[帯]・ﾌﾟﾗ[袋]"),
    /** 紙[台紙]・ﾌﾟﾗ[袋]. */
    PAPER_BOARD_PLASTIC_BAG("紙[台紙]・ﾌﾟﾗ[袋]"),
    /** ﾌﾟﾗ[袋]. */
    PAPER_PLASTIC_BAG("ﾌﾟﾗ[袋]"),;

    /**
     * value.
     */
    private final String value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    FukukitaruRecycleCodeType(final String value) {
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
    public static Optional<FukukitaruRecycleCodeType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static String convertToValue(final FukukitaruRecycleCodeType type) {
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
    public static FukukitaruRecycleCodeType convertToType(final String value) {
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
    public static FukukitaruRecycleCodeType convertToJson(final String value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
