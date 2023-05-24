package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 採番マスタのカラム名の定義.
 */
public enum MNumberColumnNameType {

    /** 発注No. */
    ORDER_NUMBER("order_number"),
    /** 納品No. */
    DELIVERY_NUMBER("delivery_number"),
    /** 納品依頼No. */
    DELIVERY_REQUEST_NUMBER("delivery_request_number"),
    /** 管理No. */
    SQ_MANAGE_NUMBER("sq_manage_number"),
    /** 仕入伝票No. */
    PURCHASE_VOUCHER_NUMBER("purchase_voucher_number"),
    /** 指示番号. */
    INSTRUCT_NUMBER("instruct_number"),
    /** 伝票番号. */
    VOUCHER_NUMBER("voucher_number"),
    /** 管理No. */
    MANAGE_NUMBER("manage_number"),
    /** 出荷伝票No. */
    SHIPMENT_VOUCHER_NUMBER("shipment_voucher_number"),
    /** 指示管理番号. */
    INSTRUCT_MANAGE_NUMBER("instruction_manage_number");

    /**
     * value.
     */
    private final String value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    MNumberColumnNameType(final String value) {
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
    public static Optional<MNumberColumnNameType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     *
     * @param type type
     * @return value
     */
    public static String convertToValue(final MNumberColumnNameType type) {
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
    public static MNumberColumnNameType convertToType(final String value) {
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
    public static MNumberColumnNameType convertToJson(final String value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
