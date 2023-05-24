package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.DeliveryAtLateType;

/**
 * 納期遅延区分の型変換.
 */
@Converter
public class DeliveryAtLateTypeConverter implements AttributeConverter<DeliveryAtLateType, String> {

    @Override
    public String convertToDatabaseColumn(final DeliveryAtLateType attribute) {
        return DeliveryAtLateType.convertToValue(attribute);
    }

    @Override
    public DeliveryAtLateType convertToEntityAttribute(final String dbData) {
        return DeliveryAtLateType.convertToType(dbData);
    }
}
