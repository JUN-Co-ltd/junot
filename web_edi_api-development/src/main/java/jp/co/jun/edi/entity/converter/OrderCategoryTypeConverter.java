package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.OrderCategoryType;

/**
 * 発注分類区分の型変換.
 */
@Converter
public class OrderCategoryTypeConverter implements AttributeConverter<OrderCategoryType, String> {

    @Override
    public String convertToDatabaseColumn(final OrderCategoryType attribute) {
        return OrderCategoryType.convertToValue(attribute);
    }

    @Override
    public OrderCategoryType convertToEntityAttribute(final String dbData) {
        return OrderCategoryType.convertToType(dbData);
    }
}
