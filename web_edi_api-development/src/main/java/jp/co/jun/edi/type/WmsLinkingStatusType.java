package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * WMS連携ステータスの定義.
 */
public enum WmsLinkingStatusType {
    // 0X系：送信
    /** 00 : ファイル未作成. */
    FILE_NOT_CREATE("00"),
    /** 01 : ファイル作成中. */
    FILE_CREATING("01"),
    /** 02 : ファイル作成済み. */
    FILE_CREATED("02"),
    /** 03 : ファイル作成エラー. */
    FILE_CREATE_ERROR("03"),
    /** 04 : FTP送信中. */
    SENDING("04"),
    /** 05 : FTP送信済み. */
    SEND_ALREADY("05"),
    /** 06 : FTP送信エラー. */
    SEND_ERROR("06"),

    // 1X系：受信
    /** 10 : ファイル未取込. */
    FILE_NOT_IMPORT("10"),
    /** 11 : ファイル取込中. */
    FILE_IMPORTING("11"),
    /** 12 : ファイル取込済. */
    FILE_IMPORTED("12"),
    /** 13 : ファイル取込エラー. */
    FILE_IMPORT_ERROR("13");

    /**
     * value.
     */
    private final String value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    WmsLinkingStatusType(final String value) {
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
    public static Optional<WmsLinkingStatusType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type type
     * @return value
     */
    public static String convertToValue(final WmsLinkingStatusType type) {
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
    public static WmsLinkingStatusType convertToType(final String value) {
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
    public static WmsLinkingStatusType convertToJson(final String value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
