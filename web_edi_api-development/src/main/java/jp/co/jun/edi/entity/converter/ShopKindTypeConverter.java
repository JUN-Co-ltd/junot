package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.ShopKindType;

/**
 * 店舗区分の型変換.
 */
@Converter
public class ShopKindTypeConverter implements AttributeConverter<ShopKindType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final ShopKindType attribute) {
        return ShopKindType.convertToValue(attribute);
    }

    @Override
    public ShopKindType convertToEntityAttribute(final Integer dbData) {
        return ShopKindType.convertToType(dbData);
    }

}
