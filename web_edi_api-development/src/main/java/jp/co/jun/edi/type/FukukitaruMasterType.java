package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * フクキタルマスタ種別の定義.
 */
public enum FukukitaruMasterType {

    /** テープ巾(1). */
    TAPE_WIDTH(1),

    /** テープ種類(2). */
    TAPE_TYPE(2),

    /** 洗濯ネーム付記用語(3). */
    WASH_NAME_APPENDICES_TERM(3),

    /** アテンションタグ付記用語(4). */
    ATTENTION_TAG_APPENDICES_TERM(4),

    /** アテンションシールのシール種類(5). */
    ATTENTION_TAG_SEAL(5),

    /** リサイクルマーク(6). */
    RECYCL(6),

    /** 中国内販情報製品分類(7). */
    CN_DERIVERY_PRODUCT_CATEGORY(7),

    /** 中国内販情報製品種別(8). */
    CN_DERIVERY_PRODUCT_TYPE(8),

    /** アテンションタグ(9). */
    ATTENTION_TAG(9),

    /** アテンションネーム(10). */
    ATTENTION_NAME(10),

    /** 同封副資材(11). */
    AUXILIARY_MATERIAL(11),

    /** 下札類(12). */
    HANG_TAG(12),

    /** 洗濯マーク(13). */
    WASH_PATTERN(13),

    /** カテゴリコード(14). */
    CATEGORY_CODE(14),

    /** サスティナブルマーク(15). */
    SUSTAINABLE_MARK(15);

    /**
     * value.
     */
    private final Integer value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    FukukitaruMasterType(final Integer value) {
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
    public static Optional<FukukitaruMasterType> findByValue(final Integer value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final FukukitaruMasterType type) {
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
    public static FukukitaruMasterType convertToType(final Integer value) {
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
    public static FukukitaruMasterType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        Integer valInt = Integer.parseInt(value);

        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
