package jp.co.jun.edi.util;

/**
 * メールフォーマットユーティリティ.
 * ※velocityのカスタムtoolで使用する為staticクラスにしないでください.
 */
public class MailFormatUtil {

    /** コンストラクタ. */
    public MailFormatUtil() {
    }

    /**
     * 品番にハイフンを付けて返す.
     *
     * @param partNo 品番データ
     * @return 整形した品番
     */
    public String formatPartNo(final String partNo) {
        return BusinessUtils.formatPartNo(partNo);
    }
}
