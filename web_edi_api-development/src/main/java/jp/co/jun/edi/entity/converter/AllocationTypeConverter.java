package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.AllocationType;

/**
 * 配分区分の型変換.
 */
@Converter
public class AllocationTypeConverter implements AttributeConverter<AllocationType, String> {

    @Override
    public String convertToDatabaseColumn(final AllocationType attribute) {
        return AllocationType.convertToValue(attribute);
    }

    @Override
    public AllocationType convertToEntityAttribute(final String dbData) {
        return AllocationType.convertToType(dbData);
    }
}
