//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.type;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 仕入実績区分の定義.
 */
public enum PurchaseRecordType {
	/** 未選択 */
	 NO_SELECT(0),
    /** 追加仕入. */
    ADDITIONAL_PURCHASE(1),
    /** 仕入返品. */
    RETURN_PURCHASE(2),
    /** 店舗発注分仕入. */
    STORE_PURCAHSE(3),
    /** 消化委託店舗仕入. */
    STORE_DIGESTION_COMMISSION(4);
    /**
     * value.
     */
    private final int value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    PurchaseRecordType(final int value) {
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
    public static Optional<PurchaseRecordType> findByValue(final int value) {
        return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final PurchaseRecordType type) {
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
    public static PurchaseRecordType convertToType(final Integer value) {
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
    public static PurchaseRecordType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        final Integer valInt = Integer.parseInt(value);
        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(value));
    }
}
//PRD_0133 #10181 add JFE end