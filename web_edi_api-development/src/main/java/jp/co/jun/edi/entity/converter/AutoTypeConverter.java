package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.AutoType;

/**
 * 自動区分の型変換.
 */
@Converter
public class AutoTypeConverter implements AttributeConverter<AutoType, String> {

    @Override
    public String convertToDatabaseColumn(final AutoType attribute) {
        return AutoType.convertToValue(attribute);
    }

    @Override
    public AutoType convertToEntityAttribute(final String dbData) {
        return AutoType.convertToType(dbData);
    }
}
