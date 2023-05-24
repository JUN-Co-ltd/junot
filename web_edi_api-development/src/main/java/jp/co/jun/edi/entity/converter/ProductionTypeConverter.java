package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.ProductionType;

/**
 * 自動区分の型変換.
 */
@Converter
public class ProductionTypeConverter implements AttributeConverter<ProductionType, String> {

    @Override
    public String convertToDatabaseColumn(final ProductionType attribute) {
        return ProductionType.convertToValue(attribute);
    }

    @Override
    public ProductionType convertToEntityAttribute(final String dbData) {
        return ProductionType.convertToType(dbData);
    }
}
