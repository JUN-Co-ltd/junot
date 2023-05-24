package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * メール分類.
 */
public enum MMailCodeType {
    /** 商品登録(0). */
    ITEM_REGIST(0),
    /** 品番登録(1). */
    PART_REGIST(1),
    /** 発注登録(2). */
    ORDER_REGIST(2),
    /** 納品依頼登録(3). */
    DELIVERY_REGIST(3),
    /** 納品依頼承認(4). */
    DELIVERY_APPROVED(4),
    /** 納品予定登録(5). */
    DELIVERY_PLAN_REGIST(5),
    /** 納品予定更新(6). */
    DELIVERY_PLAN_UPDATE(6),
    /** 発注確定(7). */
    ORDER_CONFIRMED(7),
    /** フクキタル発注(8). */
    ORDER_FUKUKITARU(8),
    /** 受注確定情報[即時](11). */
    ORDER_CONFIRMED_IMMEDIATE(11),
    /** 納品依頼情報[即時](12). */
    DELIVERY_APPROVED_IMMEDIATE(12),
    /** 発注書PDF発行[夜間正式番](13). */
    PURCHASE_ORDER_OFFICIAL(13),
    /** 納品依頼情報[夜間](14). */
    DELIVERY_APPROVED_OFFICIAL(14),
    /** 発注承認(15). */
    ORDER_APPROVED(15),
    /** 発注書PDF発行[即時](16). */
    PURCHASE_ORDER_IMMEDIATE(16),
    /** 優良誤認承認更新(17). */
    MISLEADING_REPRESENTATION_UPDATE(17),
    /** 資材発注連携エラーメール転送(18). */
    MATERIAL_ORDER_LINKING_ERROR_MAIL_FORWARD(18),
	//PRD_0134 #10654 add JEF start
    /** 受領書メール送信(20). */
    PURCHASE_ITEM_SEND_MAIL(20),
	//PRD_0134 #10654 add JEF end
    // PRD_0143 #10423 JFE add start
    /** TAGDAT(19). */
    TAGDAT(19);
	// PRD_0143 #10423 JFE add end
    /**
     * value.
     */
    private final int value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    MMailCodeType(final int value) {
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
     * @return Optional<MMailCodeType>
     */
    public static Optional<MMailCodeType> findByValue(final int value) {
        return Arrays.stream(values()).filter(v -> v.value == value).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static Integer convertToValue(final MMailCodeType type) {
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
    public static MMailCodeType convertToType(final Integer value) {
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
    public static MMailCodeType convertToJson(final String value) {
        if (Objects.isNull(value)) {
            return null;
        }

        Integer valInt = Integer.parseInt(value);
        return findByValue(valInt).orElseThrow(() -> new IllegalArgumentException(value));
    }
}
