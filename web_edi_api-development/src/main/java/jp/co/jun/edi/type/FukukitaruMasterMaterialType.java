package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * フクキタル用資材種別の定義.
 */
public enum FukukitaruMasterMaterialType {
    /** 洗濯ネーム(1). */
    WASH_NAME(1),
    /** アテンションネーム(2). */
    ATTENTION_NAME(2),
    /** 洗濯同封副資材(3). */
    WASH_AUXILIARY_MATERIAL(3),
    /** 下札(4). */
    HANG_TAG(4),
    /** アテンションタグ(5). */
    ATTENTION_TAG(5),
    /** アテンション下札(6). */
    ATTENTION_HANG_TAG(6),
    /** NERGY用メリット下札(7). */
    HANG_TAG_NERGY_MERIT(7),
    /** 下札同封副資材(8). */
    HANG_TAG_AUXILIARY_MATERIAL(8);

    /**
     * value.
     */
    private final Integer value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    FukukitaruMasterMaterialType(final Integer value) {
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
    public static Optional<FukukitaruMasterMaterialType> findByValue(final Integer value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final FukukitaruMasterMaterialType type) {
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
    public static FukukitaruMasterMaterialType convertToType(final Integer value) {
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
    public static FukukitaruMasterMaterialType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        Integer valInt = Integer.parseInt(value);

        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
