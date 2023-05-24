package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.CompleteOrderType;

/**
 * 完納区分の型変換.
 */
@Converter
public class CompleteOrderTypeConverter implements AttributeConverter<CompleteOrderType, String> {

    @Override
    public String convertToDatabaseColumn(final CompleteOrderType attribute) {
        return CompleteOrderType.convertToValue(attribute);
    }

    @Override
    public CompleteOrderType convertToEntityAttribute(final String dbData) {
        return CompleteOrderType.convertToType(dbData);
    }
}
