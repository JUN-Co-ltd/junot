package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * サブシーズンの定義.
 */
public enum SubSeasonCodeType {
    /** 春. */
    A1("1", "A1", "A"),
    /** 夏. */
    A2("2", "A2", "A"),
    /** 秋. */
    B1("5", "B1", "B"),
    /** 冬. */
    B2("6", "B2", "B"),
    /** 年間. */
    C("9", "C", "C");

    /**
     * value.
     */
    private final String value;

    /**
     * 画面のシーズンコード.
     */
    private final String screenSeasonCode;

    /**
     * DBのシーズンコード.
     */
    private final String dbSeasonCode;

    /**
     * コンストラクタ.
     *
     * @param value value
     * @param screenSeasonCode 画面のシーズンコード
     * @param dbSeasonCode DBのシーズンコード
     */
    SubSeasonCodeType(final String value, final String screenSeasonCode, final String dbSeasonCode) {
        this.value = value;
        this.screenSeasonCode = screenSeasonCode;
        this.dbSeasonCode = dbSeasonCode;
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
     * 画面のシーズンコードを取得する.
     *
     * @return 画面のシーズンコード
     */
    public String getScreenSeasonCode() {
        return screenSeasonCode;
    }

    /**
     * DBのシーズンコードを取得する.
     *
     * @return DBのシーズンコード
     */
    public String getDbSeasonCode() {
        return dbSeasonCode;
    }

    /**
     * valueからtypeを検索する.
     *
     * @param value value
     * @return Optional<SubSeasonCodeType>
     */
    public static Optional<SubSeasonCodeType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * 画面のシーズンコードからtypeを検索する.
     *
     * @param screenSeasonCode 画面のシーズンコード
     * @return Optional<SubSeasonCodeType>
     */
    public static Optional<SubSeasonCodeType> findByScreenSeasonCode(final String screenSeasonCode) {
        return Arrays.stream(values()).filter(v -> v.screenSeasonCode.equals(screenSeasonCode)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static String convertToValue(final SubSeasonCodeType type) {
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
    public static SubSeasonCodeType convertToType(final String value) {
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
    public static SubSeasonCodeType convertToJson(final String value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
