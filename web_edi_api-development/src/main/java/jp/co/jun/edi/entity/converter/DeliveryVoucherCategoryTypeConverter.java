package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.DeliveryVoucherCategoryType;

/**
 * 納品伝票分類の型変換.
 */
@Converter
public class DeliveryVoucherCategoryTypeConverter implements AttributeConverter<DeliveryVoucherCategoryType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final DeliveryVoucherCategoryType attribute) {
        return DeliveryVoucherCategoryType.convertToValue(attribute);
    }

    @Override
    public DeliveryVoucherCategoryType convertToEntityAttribute(final Integer dbData) {
        return DeliveryVoucherCategoryType.convertToType(dbData);
    }

}
