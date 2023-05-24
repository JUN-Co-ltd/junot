package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.OrderSheetOutType;

/**
 * 発注書出力フラグの型変換.
 */
@Converter
public class OrderSheetOutTypeConverter implements AttributeConverter<OrderSheetOutType, Boolean> {

    @Override
    public Boolean convertToDatabaseColumn(final OrderSheetOutType attribute) {
        return OrderSheetOutType.convertToValue(attribute);
    }

    @Override
    public OrderSheetOutType convertToEntityAttribute(final Boolean dbData) {
        return OrderSheetOutType.convertToType(dbData);
    }
}
