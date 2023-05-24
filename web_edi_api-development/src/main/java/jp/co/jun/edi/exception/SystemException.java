package jp.co.jun.edi.exception;

import org.springframework.util.Assert;

import jp.co.jun.edi.type.MessageCodeType;

/**
 * システムエラーの場合の例外クラス.
 */
public class SystemException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * exception code.
     */
    private final MessageCodeType code;

    /**
     * Constructor.
     *
     * @param code exception code
     * @param message the detail message
     * @param cause {@link Throwable} instance
     */
    public SystemException(final MessageCodeType code, final String message, final Throwable cause) {
        super(message, cause);

        Assert.notNull(code, "code must not be null");

        this.code = code;
    }

    /**
     * Constructor.
     *
     * @param code exception code
     * @param message the detail message
     */
    public SystemException(final MessageCodeType code, final String message) {
        this(code, message, null);
    }

    /**
     * Constructor.
     *
     * @param code exception code
     * @param cause {@link Throwable} instance
     */
    public SystemException(final MessageCodeType code, final Throwable cause) {
        this(code, null, cause);
    }

    /**
     * @return String code
     */
    public MessageCodeType getCode() {
        return code;
    }
}
