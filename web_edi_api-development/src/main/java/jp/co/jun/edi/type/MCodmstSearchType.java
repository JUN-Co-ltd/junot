package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 発注生産システムのコードマスタの検索タイプの定義.
 */
public enum MCodmstSearchType {
    /** 0 : コード/名称 部分一致. */
    LIKE_CODE_OR_NAME("0"),
    /** 1 : コード  部分一致. */
    LIKE_CODE("1"),
    /** 2 : 名称 部分一致. */
    LIKE_NAME("2"),
    /** 3 : コード 完全一致. */
    CODE("3");

    /**
     * value.
     */
    private final String value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    MCodmstSearchType(final String value) {
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
    public static Optional<MCodmstSearchType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static String convertToValue(final MCodmstSearchType type) {
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
    public static MCodmstSearchType convertToType(final String value) {
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
    public static MCodmstSearchType convertToJson(final String value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
