package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.BusinessType;

/**
 * (WMS関連)業務区分の型変換.
 */
@Converter
public class BusinessTypeConverter implements AttributeConverter<BusinessType, String> {

    @Override
    public String convertToDatabaseColumn(final BusinessType attribute) {
        return BusinessType.convertToValue(attribute);
    }

    @Override
    public BusinessType convertToEntityAttribute(final String dbData) {
        return BusinessType.convertToType(dbData);
    }
}
