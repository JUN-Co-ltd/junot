package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.ShippingInstructionDataType;

/**
 * データ種別の型変換.
 */
@Converter
public class ShippingInstructionDataTypeConverter implements AttributeConverter<ShippingInstructionDataType, String> {

    @Override
    public String convertToDatabaseColumn(final ShippingInstructionDataType attribute) {
        return ShippingInstructionDataType.convertToValue(attribute);
    }

    @Override
    public ShippingInstructionDataType convertToEntityAttribute(final String dbData) {
        return ShippingInstructionDataType.convertToType(dbData);
    }
}
