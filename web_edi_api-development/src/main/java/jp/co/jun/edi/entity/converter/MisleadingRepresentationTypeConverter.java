package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.MisleadingRepresentationType;

/**
 * 優良誤認検査対象区分の型変換.
 */
@Converter
public class MisleadingRepresentationTypeConverter implements AttributeConverter<MisleadingRepresentationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final MisleadingRepresentationType attribute) {
        return MisleadingRepresentationType.convertToValue(attribute);
    }

    @Override
    public MisleadingRepresentationType convertToEntityAttribute(final Integer dbData) {
        return MisleadingRepresentationType.convertToType(dbData);
    }

}
