package jp.co.jun.edi.api;

import org.springframework.util.Assert;

import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.ValidateModel;

/**
 * バリデーションエラーの場合の例外クラス.
 *
 * ※※APIでしか投げないこと!※※
 */
public class ValidateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** バリデーション結果Model. */
    private final ValidateModel validateModel;

    /**
     * Constructor.
     *
     * @param validateModel instance of {@link ValidateModel}
     */
    public ValidateException(final ValidateModel validateModel) {
        this(validateModel, null);
    }

    /**
     * Constructor.
     *
     * @param validateModel instance of {@link ValidateModel}
     * @param cause {@link Throwable} instance
     */
    public ValidateException(final ValidateModel validateModel, final Throwable cause) {
        super(cause);

        Assert.notNull(validateModel, "validateModel must not be null");

        this.validateModel = validateModel;
    }

    /**
     * Returns the {@link ResultMessages} instance.
     *
     * @return {@link ResultMessages} instance
     */
    public ValidateModel getValidateModel() {
        return validateModel;
    }
}
