package jp.co.jun.edi.type;

/**
 * JUNoT独自のHTTPステータス.
 *
 * org.springframework.http.HttpStatus に定義されていないHTTPステータスを指定する。
 */
public enum CustomHttpStatusType {
    /** JUNoT利用時間外HTTPエラーコード. */
    UNAVAILABLE_TIME(480, "Unavailable Time");

    private final int value;

    private final String reasonPhrase;

    /**
     * コンストラクタ.
     *
     * @param value value
     * @param reasonPhrase reasonPhrase
     */
    CustomHttpStatusType(final int value, final String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Return the integer value of this status code.
     * @return this status code.
     */
    public int value() {
        return this.value;
    }

    /**
     * Return the reason phrase of this status code.
     * @return this reason phrase of this status code.
     */
    public String getReasonPhrase() {
        return this.reasonPhrase;
    }
}
