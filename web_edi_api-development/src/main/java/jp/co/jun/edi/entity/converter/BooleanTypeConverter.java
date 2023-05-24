package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.BooleanType;

/**
 * Boolean型の型変換.
 */
@Converter
public class BooleanTypeConverter implements AttributeConverter<BooleanType, Boolean> {

    @Override
    public Boolean convertToDatabaseColumn(final BooleanType attribute) {
        return BooleanType.convertToValue(attribute);
    }

    @Override
    public BooleanType convertToEntityAttribute(final Boolean dbData) {
        return BooleanType.convertToType(dbData);
    }

}
