package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 外部連携区分の定義.
 *
 * <p>
 * 1以外の値が何が設定されるか未確定のため、1のみ定義する。
 * 設定値が不明のため、AttributeConverterや@JsonValue、@JsonCreatorを利用しないこと。
 * </p>
 * <pre>
 * 1:JUNoT登録
 * 1以外:JUNoT以外で登録
 * </pre>
 */
public enum ExternalLinkingType {
    /** 1:JUNoT登録. */
    JUNOT("1");

    /**
     * value.
     */
    private final String value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    ExternalLinkingType(final String value) {
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
    public static Optional<ExternalLinkingType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type type
     * @return value
     */
    public static String convertToValue(final ExpenseItemType type) {
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
    public static ExternalLinkingType convertToType(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        return findByValue(value).orElse(null);
    }
}
