package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 全店配分削除APIレスポンスステータス区分.
 */
public enum SpecialtyQubeDeleteStatusType {
    /** 削除OK. */
    DELETE_OK(0),
    /** 削除NG. */
    DELETE_NG(1),
    /** データ無し. */
    NO_DATA(2),
    /** 削除済. */
    ALREADY_DELETED(3),
    /** その他エラー. */
    OTHER_ERROR(9);

    /**
     * value.
     */
    private final int value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    SpecialtyQubeDeleteStatusType(final int value) {
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
    public static Optional<SpecialtyQubeDeleteStatusType> findByValue(final int value) {
        return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final SpecialtyQubeDeleteStatusType type) {
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
    public static SpecialtyQubeDeleteStatusType convertToType(final Integer value) {
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
    public static SpecialtyQubeDeleteStatusType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        Integer valInt = Integer.parseInt(value);
        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(value));
    }
}
