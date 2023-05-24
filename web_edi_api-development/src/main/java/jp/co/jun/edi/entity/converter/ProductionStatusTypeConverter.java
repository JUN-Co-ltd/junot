package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.ProductionStatusType;

/**
 * 生産ステータスの型変換.
 */
@Converter
public class ProductionStatusTypeConverter implements AttributeConverter<ProductionStatusType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final ProductionStatusType attribute) {
        return ProductionStatusType.convertToValue(attribute);
    }

    @Override
    public ProductionStatusType convertToEntityAttribute(final Integer dbData) {
        return ProductionStatusType.convertToType(dbData);
    }

}
