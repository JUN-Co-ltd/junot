package jp.co.jun.edi.exception;

import jp.co.jun.edi.message.ResultMessages;

/**
 * 対象データなしの場合の例外クラス.
 */
public class ResourceNotFoundException extends ResultMessagesNotificationException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public ResourceNotFoundException() {
        this(ResultMessages.error(), null);
    }

    /**
     * Constructor.
     *
     * @param messages instance of {@link ResultMessages}
     */
    public ResourceNotFoundException(final ResultMessages messages) {
        this(messages, null);
    }

    /**
     * Constructor.
     *
     * @param cause {@link Throwable} instance
     */
    public ResourceNotFoundException(final Throwable cause) {
        this(ResultMessages.error(), cause);
    }

    /**
     * Constructor.
     *
     * @param messages instance of {@link ResultMessages}
     * @param cause {@link Throwable} instance
     */
    public ResourceNotFoundException(final ResultMessages messages, final Throwable cause) {
        super(messages, cause);
    }
}
