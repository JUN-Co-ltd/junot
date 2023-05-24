package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 納品依頼用画面項目種別の定義.
 */
public enum ScreenSettingDeliveryMasterType {
    /** 閾値. */
    THRESHOLD(1),
    /** 店舗マスタ. */
    TNPMST(2),
    /** 店舗別配分率. */
    STORE_HRTMST(3),
	// PRD_0031 add SIT start
	/** 在庫数.*/
	SHOP_STOCK(4),
	// PRD_0031 add SIT end
	// PRD_0123 #7054 mod JFE start
//	// PRD_0033 add SIT start
//	/** 売上数.*/
//	POS_SALES_QUANTITY(5);
//    // PRD_0033 add SIT end
	// PRD_0033 add SIT start
	/** 売上数.*/
	POS_SALES_QUANTITY(5),
    // PRD_0033 add SIT end
	/** 納入場所*/
	DELIVERY_LOCATION(6);
	// PRD_0123 #7054 mod JFE end
    /**
     * value.
     */
    private final int value;

    /**
     * コンストラクタ.
     *
     * @param value
     *            value
     */
    ScreenSettingDeliveryMasterType(final int value) {
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
     * @param value
     *            value
     * @return Optional<EnabledType>
     */
    public static Optional<ScreenSettingDeliveryMasterType> findByValue(final int value) {
        return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type
     *            type
     * @return value
     */
    public static Integer convertToValue(final ScreenSettingDeliveryMasterType type) {
        if (Objects.isNull(type)) {
            return null;
        }
        return type.getValue();
    }

    /**
     * value を type に変換する. value が null の場合、null を返却する.
     *
     * @param value
     *            value
     * @return type
     */
    public static ScreenSettingDeliveryMasterType convertToType(final Integer value) {
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
     * @param value
     *            value
     * @return ResultType
     */
    @JsonCreator
    public static ScreenSettingDeliveryMasterType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        Integer valInt = Integer.parseInt(value);
        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(value));
    }
}
