package jp.co.jun.edi.message;

import java.io.Serializable;
import java.util.Arrays;

import org.springframework.util.Assert;

import jp.co.jun.edi.type.MessageCodeType;

/**
 * 実行結果を格納するクラス.
 */
public final class ResultMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * メッセージコード.
     */
    private final MessageCodeType code;

    /**
     * メッセージの引数.
     */
    private final Object[] args;

    /**
     * 対象のリソース.
     *
     * <p>SKUや組成などを識別するために設定する。</p>
     */
    private String resource = null;

    /**
     * 対象のフィールド.
     *
     * <p>messages.properties の field.xxx を示す。</p>
     */
    private String field = null;

    /**
     * 対象の値.
     */
    private Object value = null;

    /**
     * Constructor.
     *
     * @param code message code
     * @param args replacement values of message format
     */
    private ResultMessage(final MessageCodeType code, final Object[] args) {
        this.code = code;
        this.args = args;
    }

    /**
     * create <code>ResultMessage</code>.
     *
     * @param code message code (must not be null)
     * @param args replacement values of message format
     * @return {@link ResultMessages} instance
     */
    public static ResultMessage fromCode(final MessageCodeType code, final Object... args) {
        Assert.notNull(code, "code must not be null");
        return new ResultMessage(code, args);
    }

    /**
     * returns code.
     *
     * @return code
     */

    public MessageCodeType getCode() {
        return code;
    }

    /**
     * returns args.
     *
     * @return args
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * エラーが発生した対象のリソースを設定.
     *
     * @param resource 対象のリソース
     * @return {@link ResultMessages} instance
     */
    public ResultMessage resource(final String resource) {
        this.resource = resource;

        return this;
    }

    /**
     * returns resource.
     *
     * @return resource
     */

    public String getResource() {
        return resource;
    }

    /**
     * エラーが発生した対象のフィールドを設定.
     *
     * @param field 対象のフィールド
     * @return {@link ResultMessages} instance
     */
    public ResultMessage field(final String field) {
        this.field = field;

        return this;
    }

    /**
     * returns field.
     *
     * @return field
     */

    public String getField() {
        return field;
    }

    /**
     * エラーが発生した対象の値を設定.
     *
     * @param value 対象の値
     * @return {@link ResultMessages} instance
     */
    public ResultMessage value(final Object value) {
        this.value = value;

        return this;
    }

    /**
     * returns value.
     *
     * @return value
     */

    public Object getValue() {
        return value;
    }

    /**
     * Outputs code and text in the {@code toString()} method of {@code ResultMessage}.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ResultMessage [code=" + code + ", args=" + Arrays.toString(args) + ", field=" + field + "]";
    }
}
