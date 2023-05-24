package jp.co.jun.edi.exception;

import org.springframework.util.Assert;

import jp.co.jun.edi.message.ResultMessages;

/**
 * 実行結果通知用の例外クラス.
 */
public abstract class ResultMessagesNotificationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 結果メッセージのリスト. */
    private final ResultMessages resultMessages;

    /**
     * Constructor.
     */
    public ResultMessagesNotificationException() {
        this(ResultMessages.error(), null);
    }

    /**
     * Constructor.
     *
     * @param cause {@link Throwable} instance
     */
    public ResultMessagesNotificationException(final Throwable cause) {
        this(ResultMessages.error(), cause);
    }

    /**
     * Constructor.
     *
     * @param messages instance of {@link ResultMessages}
     */
    public ResultMessagesNotificationException(final ResultMessages messages) {
        this(messages, null);
    }

    /**
     * Constructor.
     *
     * @param messages instance of {@link ResultMessages}
     * @param cause {@link Throwable} instance
     */
    public ResultMessagesNotificationException(final ResultMessages messages, final Throwable cause) {
        super(cause);

        Assert.notNull(messages, "messages must not be null");

        this.resultMessages = messages;
    }

    /**
     * Returns the messages in String format.
     *
     * @return String messages
     */
    @Override
    public String getMessage() {
        return resultMessages.toString();
    }

    /**
     * Returns the {@link ResultMessages} instance.
     *
     * @return {@link ResultMessages} instance
     */
    public ResultMessages getResultMessages() {
        return resultMessages;
    }
}
