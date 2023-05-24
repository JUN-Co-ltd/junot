package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.PurchaseType;

/**
 * 仕入区分の型変換.
 */
@Converter
public class PurchaseTypeConverter implements AttributeConverter<PurchaseType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final PurchaseType attribute) {
        return PurchaseType.convertToValue(attribute);
    }

    @Override
    public PurchaseType convertToEntityAttribute(final Integer dbData) {
        return PurchaseType.convertToType(dbData);
    }

}
