package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.ShippingCategoryType;

/**
 * 出荷区分の型変換.
 */
@Converter
public class ShippingCategoryTypeConverter implements AttributeConverter<ShippingCategoryType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final ShippingCategoryType attribute) {
        return ShippingCategoryType.convertToValue(attribute);
    }

    @Override
    public ShippingCategoryType convertToEntityAttribute(final Integer dbData) {
        return ShippingCategoryType.convertToType(dbData);
    }
}
