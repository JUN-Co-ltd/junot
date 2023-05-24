package jp.co.jun.edi.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 発注生産システムのコードマスタのテーブルIDの定義.
 */
public enum MCodmstTblIdType {
    /** 02 : ブランド. */
    BRAND("02"),
    /** 03 : アイテム. */
    ITEM("03"),
    /** 05 : 原産国. */
    ORIGIN_COUNTRY("05"),
    /** 07 : シーズン. */
    SEASON("07"),
    /** 09 : サイズ. */
    SIZE("09"),
    /** 10 : カラー. */
    COLOR("10"),
    /** 13 : 組成. */
    COMPOSITION("13"),
    //PRD_0136 #10671 add start
    /** 21 : 納品先. */
    DELIVERY_LOCATION("21"),
    //PRD_0136 #10671 add end
    /** 22 : 社員. */
    STAFF("22"),
    /** 25 : 配分課. */
    ALLOCATION("25"),
    /** 41 : 費目. */
    EXPENSE_ITEM("41"),
    //PRD_0136 #10671 add start
    /** 42 : 仕入伝票区分. */
    PURCHASE_VOUCHER_TYPE("42"),
    /** 43 : 売上伝票区分. */
    EARNINGS_VOUCHER_TYPE("43"),
    /** 44 : 運送会社. */
    TRUCKING_COMPANY("44"),
    /** 45 : Eコマース対象店舗. */
    E_COMMERCE_SUBJECT("45"),
    //PRD_0136 #10671 add end
    /** 49 : 変換店舗コード. */
    CONVERSION_SHOP_CODE("49"),
    //PRD_0136 #10671 add start
    /** 61 : 会社. */
    COMPANY("61"),
    //PRD_0136 #10671 add end
    /** 62 : 発注生産のメニュー. */
    MENU("62"),
    /** 63 : ゾーン. */
    ZONE("63"),
    /** 65 : 丸井デプトブランド. */
    MARUI_DEPT("65"),
    /** 67 : 丸井品番. */
    MARUI_ITEM("67"),
    /** 83 : 展開. */
    OUTLET("83"),
    /** 85 : 表示課. */
    DISTRIBUTION_SECTION("85"),
    /** B2 : サブブランド. */
    SUB_BRAND("B2"),
    /** B3 : テイスト. */
    TASTE("B3"),
    /** B7 : 素材. */
    MATERIAL("B7"),
    //PRD_0136 #10671 add start
    /** CR : 店舗クレジット会社. */
    STORE_CREDIT_COMPANY("CR"),
    //PRD_0136 #10671 add end
    /** D4 : タイプ1. */
    TYPE_1("D4"),
    /** D5 : タイプ2. */
    TYPE_2("D5"),
    /** D6 : タイプ3. */
    TYPE_3("D6"),
    //PRD_0136 #10671 add start
    /** DO : 品番削除対象外アイテム. */
    NOT_APPLICABLE_ITEM("DO"),
    /** EL : ECロジザード連携店舗. */
    EC_LOGIZARD_STORE("EL"),
    /** HY : 評価滅区分. */
    WRITE_DOWN_TYPE("HY"),
    //PRD_0136 #10671 add end
    /** JG : 事業部. */
    DIVISION("JG"),
    //PRD_0136 #10671 add start
    /** SS : サブシーズン. */
    SUB_SEASON("SS"),
    //PRD_0136 #10671 add end
    /** VB : Voi展開ブランド. */
    VOI_BRAND("VB"),
    /** VO : Voi区分情報. */
    //PRD_0136 #10671 mod start
//    VOI_SECTION("VO");
    VOI_SECTION("VO"),
    /** ZZ : 在庫管理区分. */
    INVENTORY_TYPE("ZK");
    //PRD_0136 #10671 mod end
    /**
     * value.
     */
    private final String value;

    /**
     * コンストラクタ.
     *
     * @param value value
     */
    MCodmstTblIdType(final String value) {
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
    public static Optional<MCodmstTblIdType> findByValue(final String value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst();
    }

    /**
     * type を value に変換する.type が null の場合、null を返却する.
     * @param type type
     * @return value
     */
    public static String convertToValue(final MCodmstTblIdType type) {
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
    public static MCodmstTblIdType convertToType(final String value) {
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
    public static MCodmstTblIdType convertToJson(final String value) {
        return findByValue(value).orElseThrow(() -> new IllegalArgumentException(String.valueOf(value)));
    }
}
