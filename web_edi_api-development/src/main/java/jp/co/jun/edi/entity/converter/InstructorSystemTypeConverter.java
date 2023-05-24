package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.InstructorSystemType;

/**
 * 指示元システムの型変換.
 */
@Converter
public class InstructorSystemTypeConverter implements AttributeConverter<InstructorSystemType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final InstructorSystemType attribute) {
        return InstructorSystemType.convertToValue(attribute);
    }

    @Override
    public InstructorSystemType convertToEntityAttribute(final Integer dbData) {
        return InstructorSystemType.convertToType(dbData);
    }
}
