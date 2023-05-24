package jp.co.jun.edi.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import jp.co.jun.edi.validation.validator.CorrectDateValidator;

/**
 * 日付の妥当性チェック.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { CorrectDateValidator.class })
public @interface CorrectDate {

    /**
     * @return message
     */
    String message() default "{jp.co.jun.edi.validation.constraints.CorrectDate.message}";

    /**
     * @return groups
     */
    Class<?>[] groups() default {};

    /**
     * @return payload
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * チェック.
     */
    @Target({ ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {

        /**
         * @return value
         */
        CorrectDate[] value();
    }
}
