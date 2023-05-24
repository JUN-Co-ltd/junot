package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.PsType;

/**
 * PS区分の型変換.
 */
@Converter
public class PsTypeConverter implements AttributeConverter<PsType, String> {

    @Override
    public String convertToDatabaseColumn(final PsType attribute) {
        return PsType.convertToValue(attribute);
    }

    @Override
    public PsType convertToEntityAttribute(final String dbData) {
        return PsType.convertToType(dbData);
    }
}
