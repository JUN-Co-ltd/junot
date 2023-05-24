package jp.co.jun.edi.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.validation.validator.ExpenseItemTypeSubSetValidator;

/**
 * 費目の妥当性チェック.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { ExpenseItemTypeSubSetValidator.class })
public @interface ExpenseItemTypeSubSet {
    /**
     * @return 指定可能な ExpenseItemType のリスト
     */
    ExpenseItemType[] anyOf();

    /**
     * @return message
     */
    String message() default "{jp.co.jun.edi.validation.constraints.ExpenseItemTypeSubSet.message}";

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
        ExpenseItemTypeSubSet[] value();
    }
}
