package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.SaleType;

/**
 * セール対象品区分の型変換.
 */
@Converter
public class SaleTypeConverter implements AttributeConverter<SaleType, String> {

    @Override
    public String convertToDatabaseColumn(final SaleType attribute) {
        return SaleType.convertToValue(attribute);
    }

    @Override
    public SaleType convertToEntityAttribute(final String dbData) {
        return SaleType.convertToType(dbData);
    }
}
