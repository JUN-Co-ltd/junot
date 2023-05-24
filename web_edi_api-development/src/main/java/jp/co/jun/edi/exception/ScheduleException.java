package jp.co.jun.edi.exception;

/**
 * ScheduleException.
 */
public class ScheduleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * コンストラクタ.
     * @param string メッセージ
     */
    public ScheduleException(final String string) {
        super(string);
    }

    /**
     * コンストラクタ.
     * @param string メッセージ
     * @param e 例外
     */
    public ScheduleException(final String string, final Throwable e) {
        super(string, e);
    }

    /**
     * コンストラクタ.
     * @param e 例外
     */
    public ScheduleException(final Throwable e) {
        super(e);
    }

}
