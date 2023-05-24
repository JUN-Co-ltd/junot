package jp.co.jun.edi.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 数値ユーティリティ.
 */
public final class NumberUtils {

    /**
     */
    private NumberUtils() {
    }

    /**
     * nullの場合に0を返す.
     * @param num 数値
     * @return 数値
     */
    public static Integer defaultInt(final Integer num) {
        if (num == null) {
            return 0;
        }
        return num;
    }

    /**
     * nullの場合に0を返す.
     * @param num 数値
     * @return 数値
     */
    public static BigDecimal defaultInt(final BigDecimal num) {
        if (num == null) {
            return BigDecimal.ZERO;
        }
        return num;
    }

    /**
     * BigIntegerを生成
     * null、文字列の場合に0を返す.
     * @param num 数値
     * @return 数値
     */
    public static BigInteger createBigInteger(final String num) {

        if (num == null) {
            return BigInteger.ZERO;
        }

        if (!org.apache.commons.lang3.math.NumberUtils.isCreatable(num)) {
            return BigInteger.ZERO;
        }
        return org.apache.commons.lang3.math.NumberUtils.createBigInteger(num);
    }

    /**
     * Integerを生成
     * null、文字列の場合に0を返す.
     * @param num 数値
     * @return 数値
     */
    public static Integer createInteger(final String num) {

        if (num == null) {
            return 0;
        }

        if (!org.apache.commons.lang3.math.NumberUtils.isCreatable(num)) {
            return 0;
        }
        return org.apache.commons.lang3.math.NumberUtils.createInteger(num);
    }
}
