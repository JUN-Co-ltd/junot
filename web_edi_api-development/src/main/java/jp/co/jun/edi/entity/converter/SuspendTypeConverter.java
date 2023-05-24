package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.SuspendType;

/**
 * 保留区分の型変換.
 */
@Converter
public class SuspendTypeConverter implements AttributeConverter<SuspendType, String> {

    @Override
    public String convertToDatabaseColumn(final SuspendType attribute) {
        return SuspendType.convertToValue(attribute);
    }

    @Override
    public SuspendType convertToEntityAttribute(final String dbData) {
        return SuspendType.convertToType(dbData);
    }
}
