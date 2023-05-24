package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * フクキタル用画面項目種別の定義.
 */
public enum ScreenSettingFukukitaruMasterType {

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

    /** 請求先(14). */
    BILLING_ADDRESS(14),

    /** 納品先(15). */
    DELIVERY_ADDRESS(15),

    /** 発注先(16). */
    SUPPLIER_ADDRESS(16),

    /** SKU(17). */
    SKU(17),

    /** 品番情報(18). */
    ITEM(18),

    /** 発注情報(19). */
    ORDER(19),

    /** フクキタル品番情報(20). */
    FUKUKITARU_ITEM(20),

    /** 発注タイプ(21). */
    ORDER_TYPE(21),

    /** 洗濯ネーム(23). */
    WASH_NAME(23),

    /** フクキタル用ダウンロードファイル情報(22). */
    MATERIAL_FILE(22),

    /** アテンション下札(24). */
    ATTENTION_HANG_TAG(24),

    /** カテゴリコード(25). */
    CATEGORY_CODE(25),

    /** NERGYメリット下札(26). */
    HANG_TAG_NERGY_MERIT(26),

    /** 入力補助セット(27). */
    INPUT_ASSIST_SET(27),

    /** サスティナブルマーク(28). */
    SUSTAINABLE_MARK(28);

    /**
     * value.
     */
    private final int value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    ScreenSettingFukukitaruMasterType(final int value) {
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
    public static Optional<ScreenSettingFukukitaruMasterType> findByValue(final int value) {
        return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final ScreenSettingFukukitaruMasterType type) {
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
    public static ScreenSettingFukukitaruMasterType convertToType(final Integer value) {
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
    public static ScreenSettingFukukitaruMasterType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        Integer valInt = Integer.parseInt(value);
        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(value));
    }
}
