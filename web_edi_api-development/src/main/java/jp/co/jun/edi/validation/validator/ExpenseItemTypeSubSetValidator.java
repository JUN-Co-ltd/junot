package jp.co.jun.edi.validation.validator;

import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.validation.constraints.ExpenseItemTypeSubSet;

/**
 * 費目の妥当性チェック.
 */
public class ExpenseItemTypeSubSetValidator implements ConstraintValidator<ExpenseItemTypeSubSet, ExpenseItemType> {
    /** 指定可能な ExpenseItemType のリスト. */
    private List<ExpenseItemType> subsetList;

    @Override
    public void initialize(final ExpenseItemTypeSubSet constraint) {
        this.subsetList = Arrays.asList(constraint.anyOf());
    }

    @Override
    public boolean isValid(final ExpenseItemType value, final ConstraintValidatorContext context) {
        return value == null || subsetList.contains(value);
    }
}
