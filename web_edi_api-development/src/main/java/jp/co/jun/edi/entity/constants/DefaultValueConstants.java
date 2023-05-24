package jp.co.jun.edi.entity.constants;

/**
 * DBに登録する初期値.
 */
public final class DefaultValueConstants {
    // 共通系：
    /** デフォルト値"0". */
    public static final String DEFAULT_STRING_ZERO = "0";

    /** デフォルト値int 0. */
    public static final int DEFAULT_INT_ZERO = 0;

    /** 送信区分デフォルト値. */
    public static final String DEFAULT_SEND_CODE = "1";

    // 発注系：
    /** 素材デフォルト値. */
    public static final String DEFAULT_MATERIAL = "0000";

    /** 生地メーカーデフォルト値. */
    public static final String DEFAULT_MATL_MAKER_CODE = "00000";

    /** サイズ区分デフォルト値. */
    public static final String DEFAULT_SIZE_TYPE = "00";

    // 納品依頼系：
    /** 納品No・納品依頼Noデフォルト値. */
    public static final String DEFAULT_DELIVERY_NUMBER = "000000";

    /**
     */
    private DefaultValueConstants() {
    };
}
