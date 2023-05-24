package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.BusinessDayFlgType;

/**
 * 資材発注営業日の型変換.
 */
@Converter
public class BusinessDayFlgTypeConverter implements AttributeConverter<BusinessDayFlgType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final BusinessDayFlgType attribute) {
        return BusinessDayFlgType.convertToValue(attribute);
    }

    @Override
    public BusinessDayFlgType convertToEntityAttribute(final Integer dbData) {
        return BusinessDayFlgType.convertToType(dbData);
    }

}
