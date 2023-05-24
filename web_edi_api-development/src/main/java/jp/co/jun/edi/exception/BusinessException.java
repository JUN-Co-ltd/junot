package jp.co.jun.edi.exception;

import jp.co.jun.edi.message.ResultMessages;

/**
 * 業務エラーの場合の例外クラス.
 */
public class BusinessException extends ResultMessagesNotificationException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public BusinessException() {
        this(ResultMessages.error(), null);
    }

    /**
     * Constructor.
     *
     * @param messages instance of {@link ResultMessages}
     */
    public BusinessException(final ResultMessages messages) {
        this(messages, null);
    }

    /**
     * Constructor.
     *
     * @param cause {@link Throwable} instance
     */
    public BusinessException(final Throwable cause) {
        this(ResultMessages.error(), cause);
    }

    /**
     * Constructor.
     *
     * @param messages instance of {@link ResultMessages}
     * @param cause {@link Throwable} instance
     */
    public BusinessException(final ResultMessages messages, final Throwable cause) {
        super(messages, cause);
    }
}
