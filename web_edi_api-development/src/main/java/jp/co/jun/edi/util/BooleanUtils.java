package jp.co.jun.edi.util;

/**
 * Booleanユーティリティ.
 */
public final class BooleanUtils {

    /**
     */
    private BooleanUtils() {
    }

    /**
     * 値が1の場合、true を返す.
     *
     * @param value 値
     * @return boolean型
     */
    public static boolean toBoolean(final String value) {
        if (value == null) {
            return false;
        }

        return "1".equals(value);
    }
}
