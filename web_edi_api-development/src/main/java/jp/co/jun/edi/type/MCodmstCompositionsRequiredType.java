package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 発注生産システムのコードマスタの組成(混率)の必須区分の定義.
 */
public enum MCodmstCompositionsRequiredType {
    /** 0 : 組成と率が任意. */
    ANY("00"),
    /** 1 : 組成は必須. */
    REQUIRED("10"),
    /** 2 : 組成と率が必須. */
    BOTH_REQUIRED("11");


    /**
     * value.
     */
    private final String value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    MCodmstCompositionsRequiredType(final String value) {
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
    public static Optional<MCodmstCompositionsRequiredType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static String convertToValue(final MCodmstCompositionsRequiredType type) {
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
    public static MCodmstCompositionsRequiredType convertToType(final String value) {
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
    public static MCodmstCompositionsRequiredType convertToJson(final String value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
