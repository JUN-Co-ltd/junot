package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.CarryType;

/**
 * キャリー区分の型変換.
 */
@Converter
public class CarryTypeConverter implements AttributeConverter<CarryType, String> {

    @Override
    public String convertToDatabaseColumn(final CarryType attribute) {
        return CarryType.convertToValue(attribute);
    }

    @Override
    public CarryType convertToEntityAttribute(final String dbData) {
        return CarryType.convertToType(dbData);
    }
}
