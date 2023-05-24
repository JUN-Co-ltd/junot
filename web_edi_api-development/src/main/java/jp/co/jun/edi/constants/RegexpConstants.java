package jp.co.jun.edi.constants;

/**
 * 正規表現のCONSTクラス.
 */
public final class RegexpConstants {
    /**
     * メールアドレス形式（部分一致）.
     * <p>
     * HTML5の `input[type=email]` と同様の定義。<br>
     * ただし、この要件は電子メールの構文を定義するRFC5322に対して 意図的に違反している。
     * </p>
     * @see <a href="https://html.spec.whatwg.org/multipage/input.html#valid-e-mail-address">html.spec.whatwg.org - valid-e-mail-address</a>
     */
    public static final String EMAIL_PART = "[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9]"
            + "(?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*";

    /**
     * メールアドレス形式. 空文字の場合、正規表現はチェックしない.
     * <p>
     * HTML5の `input[type=email]` と同様の定義。<br>
     * ただし、この要件は電子メールの構文を定義するRFC5322に対して 意図的に違反している。
     * </p>
     * @see <a href="https://html.spec.whatwg.org/multipage/input.html#valid-e-mail-address">html.spec.whatwg.org - valid-e-mail-address</a>
     */
    public static final String EMAIL = "^$|^" + EMAIL_PART + "$";

    /**
     * メールアドレス形式（カンマ区切り）. 空文字の場合、正規表現はチェックしない.
     * <p>
     * HTML5の `input[type=email]` と同様の定義。<br>
     * ただし、この要件は電子メールの構文を定義するRFC5322に対して 意図的に違反している。
     * </p>
     * @see <a href="https://html.spec.whatwg.org/multipage/input.html#valid-e-mail-address">html.spec.whatwg.org - valid-e-mail-address</a>
     */
    public static final String EMAIL_COMMA_DELIMITED = "^$|^(?:" + EMAIL_PART + ")(?:," + EMAIL_PART + ")*$";

    /**
     * ユーザのアカウント名（ログインID）. 空文字の場合、正規表現はチェックしない.
     */
    public static final String USER_ACCOUNT_NAME = "^$|^[0-9]{6,8}$";

    /**
     * ユーザの所属会社（会社コード）. 空文字の場合、正規表現はチェックしない.
     *
     * <pre>
     * - JUN権限 6桁
     * - メーカー 5桁
     * </pre>
     */
    public static final String USER_COMPANY = "^$|^[0-9]{5,6}$";

    /**
     * 品番. 空文字の場合、正規表現はチェックしない.
     */
    public static final String PART_NO = "^$|^[A-Z]{3}[0-9]{4}[0]$";

    /**
     * 季番. 空文字の場合、正規表現はチェックしない.
     */
    public static final String SEASON_CODE = "^$|^[A-B][1-2]|[C]$";

    /**
     * 年度(2000-2999). 空文字の場合、正規表現はチェックしない.
     */
    public static final String YAER = "^$|^2[0-9]{3}$";

    /**
     * 品名. 空文字の場合、正規表現はチェックしない.
     */
    // *** Changed 2021/06/24 (Thu) by JFE ***
    //public static final String PRODUCT_NAME = "^$|^.{1,100}$";
    //public static final String PRODUCT_NAME = "^$|^[^ヮヰヱヴヵヶヷヸヹヺヽヾ\"\', ￥＂＇，＼～”’‘\\\\‐−‒–—―㎜\t]{1,100}$";
    // 2021/07/28 (Wed) BugFix 不要な半角スペースが含まれている
    // PRD_0057 mod JFE start
    public static final String PRODUCT_NAME = "^$|^[^ヮヰヱヴヵヶヷヸヹヺヽヾ\"\',￥＂＇，＼～“”’‘\\\\‐−‒–—―㎜\t]{1,100}$";
    // PRD_0057 add JFE end
    // *** Changed 2021/06/24 (Thu) by JFE ***

    /**
     * 品名カナ. 空文字の場合、正規表現はチェックしない.
     */
    public static final String PRODUCT_NAME_KANA = "^$|^[a-zA-Z0-9 !#\\$%&\\(\\)\\*\\+\\-\\./:;<=>\\?@\\[\\]\\^_`\\{\\|\\}~｡｢｣､･ｰ"
            + "ａ-ｚＡ-Ｚ０-９ァ-ロワヲン・ー　！＃＄％＆（）＊＋－．／：；＜＝＞？＠［］＾＿｀｛｜｝、。「」]{1,200}$";

    /**
     * 年と週番号. 空文字の場合、正規表現はチェックしない.
     */
    public static final String YAER_AND_WEEK = "^$|^2[0-9]{3}[0-9]{2}$";

    /**
     * 金額. 空文字の場合、正規表現はチェックしない.
     */
    public static final String PRICE = "^$|^\\u00A5[0-9]{1,3}(,[0-9]{3}){0,2}$";

    /**
     * メーカーコード. 空文字の場合、正規表現はチェックしない.
     */
    public static final String MAKER_CODE = "^$|^[0-9]{5}$";

    /**
     * 工場コード. 空文字の場合、正規表現はチェックしない.
     */
    public static final String FACTORY_CODE = "^$|^[0-9]{6}$";

    /**
     * 委託先工場. 空文字の場合、正規表現はチェックしない.
     */
    public static final String CONSIGNMENT_FACTORY = "^$|^.{1,100}$";

    /**
     * 原産国コード. 空文字の場合、正規表現はチェックしない.
     */
    public static final String COO_CODE = "^$|^[0-9]{3}$";

    /**
     * 担当コード. 空文字の場合、正規表現はチェックしない.
     */
    public static final String STAFF_CODE = "^$|^[0-9]{6}$";

    /**
     * パターンNo. 空文字の場合、正規表現はチェックしない.
     */
    public static final String PATTERN_NO = "^$|^[a-zA-Z0-9-?]{1,10}$";

    /**
     * 丸井品番. 空文字の場合、正規表現はチェックしない.
     */
    public static final String MARUI_GARMENT_NO = "^$|^[0-9]{1,8}$";

    /**
     * Voi区分. 空文字の場合、正規表現はチェックしない.
     */
    public static final String VOI_CODE = "^$|^[0-9]{1}$";

    /**
     * 素材. 空文字の場合、正規表現はチェックしない.
     */
    public static final String MATERIAL_CODE = "^$|^[0-9]{2}$";

    /**
     * ゾーン. 空文字の場合、正規表現はチェックしない.
     */
    public static final String ZONE_CODE = "^$|^[0-9]{2}$";

    /**
     * サブブランド. 空文字の場合、正規表現はチェックしない.
     */
    public static final String SUB_BRAND_CODE = "^$|^[0-9]{2}$";

    /**
     * テイスト. 空文字の場合、正規表現はチェックしない.
     */
    public static final String TASTE_CODE = "^$|^[0-9]{2}$";

    /**
     * タイプ1. 空文字の場合、正規表現はチェックしない.
     */
    public static final String TYPE1_CODE = "^$|^[0-9]{2}$";

    /**
     * タイプ2. 空文字の場合、正規表現はチェックしない.
     */
    public static final String TYPE2_CODE = "^$|^[0-9]{2}$";

    /**
     * タイプ3. 空文字の場合、正規表現はチェックしない.
     */
    public static final String TYPE3_CODE = "^$|^[0-9]{3}$";

    /**
     * 展開. 空文字の場合、正規表現はチェックしない.
     */
    public static final String OUTLET_CODE = "^$|^[0-9]{2}$";

    /**
     * フラグ. 空文字の場合、正規表現はチェックしない.
     */
    public static final String BOOLEAN = "^$|^[0-1]$";

    /**
     * メーカー品番. 空文字の場合、正規表現はチェックしない.
     */
    public static final String MAKER_GARMENT_NO = "^$|^[a-zA-Z0-9]{1,8}$";

    /**
     * 取引メモ. 空文字の場合、正規表現はチェックしない.
     */
    public static final String MEMO = "^$|^.{1,120}$";

    /**
     * 管理メモ. 空文字の場合、正規表現はチェックしない.
     */
    public static final String ITEM_MASSAGE = "^$|^.{1,50}$";

    /**
     * カラーコード（"00"以外の2桁の数字）. 空文字の場合、正規表現はチェックしない.
     */
    public static final String COLOR_CODE = "^$|^[0-9][1-9]|[1-9][0-9]$";

    /**
     * カラーコード（"00"を含む2桁の数字）. 空文字の場合、正規表現はチェックしない.
     */
    public static final String COLOR_CODE_00 = "^$|^[0-9]{2}$";

    /**
     * サイズ. 空文字の場合、正規表現はチェックしない.
     */
    public static final String SIZE = "^$|^.{1,4}$";

    /**
     * JANコード. 空文字の場合、正規表現はチェックしない.
     */
    public static final String JAN_CODE = "^$|^[0-9]{8}|[0-9]{13}$";

    /**
     * UPCコード. 空文字の場合、正規表現はチェックしない.
     */
    public static final String UPC_CODE = "^$|^[0-9]{8}|[0-9]{12}$";

    /**
     * 部位コード(0-99999). 空文字の場合、正規表現はチェックしない.
     */
    public static final String PARTS_CODE = "^$|^[0-9]|[1-9][0-9]{1,4}$";

    /**
     * 素材コード. 空文字の場合、正規表現はチェックしない.
     */
    public static final String COMPOSITION_CODE = "^$|^.{1,2}$";

    /**
     * 混率(0-100). 空文字の場合、正規表現はチェックしない.
     */
    public static final String PERCENT = "^$|^[0-9]|[1-9][0-9]|100$";

    /**
     * 他社品番. 空文字の場合、正規表現はチェックしない.
     *
     * <pre>
     * 許可する文字種
     * - a-z : 半角英小文字
     * - A-Z : 半角英大文字
     * - 0-9 : 半角数字
     * - !#$%&'()*-./:;<>?@[]^_`{|}~ : 禁止記号(*1)を除く半角記号
     *
     * (*1)禁止記号
     * -   : 半角スペース
     * - " : ダブルクォーテーション
     * - , : カンマ
     * - \ : 円マーク
     * </pre>
     */
    public static final String EXTERNAL_PART_NO = "^$|^[a-zA-Z0-9!#\\$%&'\\(\\)\\*\\+\\-\\./:;<=>\\?@\\[\\]\\^_`\\{\\|\\}~]{1,20}$";

    /**
     * 他社カラー. 空文字の場合、正規表現はチェックしない.
     */
    public static final String EXTERNAL_COLOR_CODE = "^$|^[a-zA-Z0-9]{1,20}$";

    /**
     * 他社サイズ. 空文字の場合、正規表現はチェックしない.
     */
    public static final String EXTERNAL_SIZE = "^$|^[a-zA-Z0-9\\.]{1,20}$";

    /**
     * 仕入相手伝票No. 空文字の場合、正規表現はチェックしない.
     */
    public static final String MAKER_VOUCHER_NUMBER = "^$|^[a-zA-Z0-9]{6}$";

    /**
     */
    private RegexpConstants() {
    };
}
