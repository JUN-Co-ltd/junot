package jp.co.jun.edi.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.Assert;

import jp.co.jun.edi.type.MessageCodeType;

/**
 * 結果メッセージのリストを格納するクラス.
 */
public final class ResultMessages implements Serializable, Iterable<ResultMessage> {
    private static final long serialVersionUID = 1L;

    /**
     * メッセージのタイプ.
     */
    private final ResultMessageType type;

    /**
     * メッセージのリスト.
     */
    private final List<ResultMessage> list = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param type message type
     */
    private ResultMessages(final ResultMessageType type) {
        this(type, (ResultMessage[]) null);
    }

    /**
     * Constructor.
     *
     * @param type message type
     * @param messages messages to add
     */
    private ResultMessages(final ResultMessageType type, final ResultMessage... messages) {
        Assert.notNull(type, "type must not be null");

        this.type = type;

        if (messages != null) {
            addAll(messages);
        }
    }

    /**
     * returns type.
     *
     * @return type
     */
    public ResultMessageType getType() {
        return type;
    }

    /**
     * returns messages.
     *
     * @return messages
     */
    public List<ResultMessage> getList() {
        return list;
    }

    /**
     * add a ResultMessage.
     *
     * @param message ResultMessage instance
     * @return this result messages
     */
    public ResultMessages add(final ResultMessage message) {
        Assert.notNull(message, "message must not be null");

        this.list.add(message);

        return this;
    }

    /**
     * add code to create and add ResultMessages.
     *
     * @param code message code
     * @return this result messages
     */
    public ResultMessages add(final MessageCodeType code) {
        Assert.notNull(code, "code must not be null");

        this.add(ResultMessage.fromCode(code));

        return this;
    }

    /**
     * add code and args to create and add ResultMessages.
     *
     * @param code message code
     * @param args replacement values of message format
     * @return this result messages
     */
    public ResultMessages add(final MessageCodeType code, final Object... args) {
        Assert.notNull(code, "code must not be null");

        this.add(ResultMessage.fromCode(code, args));

        return this;
    }

    /**
     * add all messages. (excludes <code>null</code> message)<br>
     * <p>
     * if <code>messages</code> is <code>null</code>, no message is added.
     * </p>
     *
     * @param messages messages to add
     * @return this messages
     */
    public ResultMessages addAll(final ResultMessage... messages) {
        Assert.notNull(messages, "messages must not be null");

        for (ResultMessage message : messages) {
            add(message);
        }

        return this;
    }

    /**
     * add all messages. (excludes <code>null</code> message)<br>
     *
     * <p>
     * if <code>messages</code> is <code>null</code>, no message is added.
     * </p>
     * @param messages messages to add
     * @return this messages
     */
    public ResultMessages addAll(final Collection<ResultMessage> messages) {
        Assert.notNull(messages, "messages must not be null");

        for (ResultMessage message : messages) {
            add(message);
        }

        return this;
    }

    /**
     * returns whether messages are not empty.
     *
     * @return whether messages are not empty
     */
    public boolean isNotEmpty() {
        return !list.isEmpty();
    }

    /**
     * factory method for success messages.
     *
     * @return success messages
     */
    public static ResultMessages success() {
        return new ResultMessages(ResultMessageType.SUCCESS);
    }

    /**
     * factory method for info messages.
     *
     * @return info messages
     */
    public static ResultMessages info() {
        return new ResultMessages(ResultMessageType.INFO);
    }

    /**
     * factory method for warning messages.
     *
     * @return warning messages
     */
    public static ResultMessages warning() {
        return new ResultMessages(ResultMessageType.WARNING);
    }

    /**
     * factory method for error messages.
     *
     * @return error messages
     */
    public static ResultMessages error() {
        return new ResultMessages(ResultMessageType.ERROR);
    }

    /**
     * factory method for fatal messages.
     *
     * @return error messages
     */
    public static ResultMessages fatal() {
        return new ResultMessages(ResultMessageType.FATAL);
    }

    /**
     * Returns {@link Iterator} instance that iterates over a list of {@link ResultMessage}.
     *
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<ResultMessage> iterator() {
        return list.iterator();
    }

    /**
     * Outputs type of messages in this {@code ResultMessages} and the list of messages itself.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ResultMessages [type=" + type + ", list=" + list + "]";
    }
}
