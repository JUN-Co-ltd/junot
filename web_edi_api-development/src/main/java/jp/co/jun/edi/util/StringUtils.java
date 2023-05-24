package jp.co.jun.edi.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Stringユーティリティ.
 */
public final class StringUtils {
    /**
     */
    private StringUtils() {
    }

    /**
     * 数値をString型に変換する.
     * @param num 数値
     * @return 整形後数値
     */
    public static String defaultString(final Integer num) {
        if (num == null) {
            return "";
        }
        return num.toString();
    }

    /**
     * 値の文字列を、空白文字（半角スペース、全角スペース）で分割する.
     * 分割する文字列がない場合は、空の配列を返却する.
     *
     * @param value 値
     * @return リスト配列
     */
    public static List<String> splitWhitespace(final String value) {
        if (!org.springframework.util.StringUtils.isEmpty(value)) {
            return Arrays.asList(org.apache.commons.lang3.StringUtils.split(value, " 　"));
        }

        return Collections.emptyList();
    }

    /**
     * textサイズ ≧ length の場合、length目を切り捨てる.
     * textサイズ ＜ length の場合、textを返す
     * <pre>
     * substring("12345678901234",13) returns "123456789012"
     * substring("1234567890123",13) returns "123456789012"
     * substring("123456789012",13) returns "123456789012"
     * substring("12345",13) returns "12345"
     * substring(null,13) returns ""
     * substring("",13) returns ""
     * </pre>
     * @param text 文字列
     * @param length 文字数
     * @return 文字列
     */
    public static String substring(final String text, final int length) {
        if (text == null) {
            return "";
        }
        if (length - 1 < 0) {
            return "";
        }

        return org.apache.commons.lang3.StringUtils.left(text, length - 1);
    }

    /**
     * 数値を文字列に変換する.
     * ただし、数値が0の場合は、空白に置換する.
     * @param value 数値
     * @return 数値を文字列に変換した情報
     */
    public static String convertIntToStringZeroIsBlank(final int value) {
        if (value == 0) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
        return String.valueOf(value);
    }

    /**
     * 文字列を指定の桁数で分割する.
     * nullまたは空白値の場合は空の配列を返却する.
     *
     * @param text 文字列
     * @param length 分割文字列
     * @return 指定桁数に分割したリスト
     */
    public static List<String> splitByLength(final String text, final int length) {

        if (org.springframework.util.StringUtils.isEmpty(text)) {
            return Collections.emptyList();
        }

        final List<String> strings = new ArrayList<String>();

        int index = 0;
        while (index < text.length()) {
            strings.add(text.substring(index, Math.min(index + length, text.length())));
            index += length;
        }

        return strings;
    }

    /**
     * 値の文字列を、空白文字（半角スペース、全角スペース）で分割し、部分一致検索用の配列を返却する.
     * 分割する文字列がない場合は、空の配列を返却する.
     *
     * @param value 値
     * @return リスト配列
     */
    public static List<String> splitWhitespaceGeneratePartialMatchList(final String value) {
        final List<String> splittedList = splitWhitespace(value);
        if (!splittedList.isEmpty()) {
            final List<String> partialMatchList = splittedList.stream().map(keyword -> "%" + keyword + "%")
                    .collect(Collectors.toList());
            return partialMatchList;
        }
        return splittedList;
    }

    /**
     * 金額の文字列から、\とカンマを削除する.
     *
     * @param value 文字列
     * @return 変換後文字列
     */
    public static String priceToNumber(final String value) {
        return org.apache.commons.lang3.StringUtils.removeAll(value, "[\\u00A5,]");
    }

    /**
     * 半角スペースを右トリムして、トリム後の値が空文字の場合は、nullを返却する.
     *
     * <pre>
     * StringUtils.rtrimToNull(null)          = null
     * StringUtils.rtrimToNull("")            = null
     * StringUtils.rtrimToNull("     ")       = null
     * StringUtils.rtrimToNull("abc")         = "abc"
     * StringUtils.rtrimToNull("    abc    ") = "    abc"
     * </pre>
     *
     * @param value 文字列
     * @return 変換後文字列
     */
    public static String rtrimToNull(final String value) {
        return org.apache.commons.lang3.StringUtils.defaultIfEmpty(org.apache.commons.lang3.StringUtils.stripEnd(value, " "), null);
    }

    /**
     * @param number 数値
     * @param length 桁数
     * @return ゼロパディングされたString
     */
    public static String toStringPadding0(final BigInteger number, final int length) {
        final String fmt = "%0" + length + "d";
        return String.format(fmt, number);
    }

    /**
     * @param number 数値
     * @param length 桁数
     * @return ゼロパディングされたString
     */
    public static String toStringPadding0(final int number, final int length) {
        final String fmt = "%0" + length + "d";
        return String.format(fmt, number);
    }

    /**
     * 文字列を比較して一致しているかどうかを確認する.
     * @param str1 文字列1
     * @param str2 文字列2
     * @return true:一致/false:不一致
     */
    public static boolean equals(final String str1, final String str2) {
        return org.apache.commons.lang3.StringUtils.equals(str1, str2);
    }

    /**
     * @param number 数値
     * @param length 桁数
     * @return +ゼロパディングされたString
     */
    public static String toStringPaddingPlus0(final BigDecimal number, final int length) {
        final String fmt = "%" + String.valueOf(length - 1) + "s";
        return "+" + String.format(fmt, number.toPlainString()).replace(" ", "0");
    }

    /**
     * 指定桁数まで右半角スペース埋め.
     *
     * @param value 対象文字列
     * @param length 桁数
     * @return 結果文字列
     */
    public static String paddingSpaceRight(final String value, final int length) {
        final String fmt = "%" + String.valueOf(-length) + "s";
      return String.format(fmt, value);
    }

    /**
     * 全角文字が含まれているか判断する.
     *
     * @param value 判定対象文字
     * @return true：全角文字あり, false：全角文字なし
     */
    public static boolean isContainFullWidth(final String value) {
        boolean isContainFull = false;
        final char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (isDoubleByte(chars[i])) {
                isContainFull = true;
                break;
            }
        }
        return isContainFull;
    }

    /**
     * 指定した文字が全角かを判定する.
     * Javaにおいては文字列は全てUNICODEで表現されるため、
     * 全角半角の判定も各文字のUNICODEで判断する
     *
     * @param source 判定対象文字
     * @return true：全角, false：半角
     */
    private static boolean isDoubleByte(final char source) {
      if ((source <= '\u007e') || // 英数字
        (source == '\u00a5') || // \記号
        (source == '\u203e') || // ~記号
        (source >= '\uff61' && source <= '\uff9f') // 半角カナ
      ) {
        // 半角
        return false;
      }
      // 全角
      return true;
    }

    /**
     * NULLの場合指定桁数まで0埋め.
     *
     * @param value 対象文字列
     * @param length 桁数
     * @return 結果文字列
     */
    public static String paddingZeroIfEmpty(final String value, final int length) {
        if (Objects.isNull(value)) {
            return toStringPadding0(BigInteger.ZERO, length);
        }
      return value;
    }
}
