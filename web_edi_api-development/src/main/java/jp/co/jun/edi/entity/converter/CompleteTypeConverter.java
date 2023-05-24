package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.CompleteType;

/**
 * 済区分の型変換.
 */
@Converter
public class CompleteTypeConverter implements AttributeConverter<CompleteType, String> {

    @Override
    public String convertToDatabaseColumn(final CompleteType attribute) {
        return CompleteType.convertToValue(attribute);
    }

    @Override
    public CompleteType convertToEntityAttribute(final String dbData) {
        return CompleteType.convertToType(dbData);
    }
}
