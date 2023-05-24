package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.PurchaseDataType;

/**
 * データ種別の型変換.
 */
@Converter
public class PurchaseDataTypeConverter implements AttributeConverter<PurchaseDataType, String> {

    @Override
    public String convertToDatabaseColumn(final PurchaseDataType attribute) {
        return PurchaseDataType.convertToValue(attribute);
    }

    @Override
    public PurchaseDataType convertToEntityAttribute(final String dbData) {
        return PurchaseDataType.convertToType(dbData);
    }
}
