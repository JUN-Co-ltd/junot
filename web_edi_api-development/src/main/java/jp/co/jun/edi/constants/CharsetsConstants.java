package jp.co.jun.edi.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 文字コードのCONSTクラス.
 */
public final class CharsetsConstants {

    /**
     * MS932(Windows-31J).
     */
    public static final Charset MS932 = Charset.forName("MS932");

    /**
     * UTF-8.
     */
    public static final Charset UTF8 = StandardCharsets.UTF_8;

    // PRD_0031 add SIT start
    /**
     * SJIS.
     */
    public static final Charset SJIS = Charset.forName("SJIS");
    // PRD_0031 add SIT end

    /**
     */
    private CharsetsConstants() {
    };
}
