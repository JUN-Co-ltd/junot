package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.ExpenseItemType;

/**
 * 費目の型変換.
 */
@Converter
public class ExpenseItemTypeConverter implements AttributeConverter<ExpenseItemType, String> {

    @Override
    public String convertToDatabaseColumn(final ExpenseItemType attribute) {
        return ExpenseItemType.convertToValue(attribute);
    }

    @Override
    public ExpenseItemType convertToEntityAttribute(final String dbData) {
        return ExpenseItemType.convertToType(dbData);
    }
}
