package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * (WMS関連)業務区分の定義.
 */
public enum BusinessType {
    /** SR : 仕入指示. */
    PURCHASE_INSTRUCTION("SR"),
    /** KR : 仕入確定. */
    PURCHASE_CONFIRM("KR"),
    /** SH : 配分出荷指示. */
    DISTRIBUTION_SHIPMENT_INSTRUCTION("SH"),
    /** KH : 配分出荷指示確定. */
    DISTRIBUTION_SHIPMENT_CONFIRM("KH"),
    /** ST : 返品指示. */
    RETURN_INSTRUCTION("ST"),
    /** KT : 返品確定. */
    RETURN_CONFIRM("KT"),
    /** HH : 補充対象品番. */
    REPLENISHMENT_ITEM("HH"),
    /** HS : 補充出荷指示. */
    REPLENISHMENT_SHIPPING_INSTRUCTION("HS"),
    /** SI : SCS・ZOZO 在庫出荷指示取込. */
    INVENTORY_IMPORT("SI"),
    /** SZ : 在庫出荷指示. */
    INVENTORY_INSTRUCTION("SZ"),
    /** QR : 直送仕入確定. */
    DIRECT_PURCHASE_CONFIRM("QR"),
    /** QH : 直送配分出荷確定. */
    DIRECT_DISTRIBUTION_SHIPMENT_CONFIRM("QH"),
    /** KZ : 在庫出荷確定. */
    INVENTORY_CONFIRM("KZ"),
    /** OR : 会計仕入確定. */
    ACCOUNT_PURCHASE_CONFIRM("OR");

    /**
     * value.
     */
    private final String value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    BusinessType(final String value) {
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
    public static Optional<BusinessType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type type
     * @return value
     */
    public static String convertToValue(final BusinessType type) {
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
    public static BusinessType convertToType(final String value) {
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
    public static BusinessType convertToJson(final String value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
