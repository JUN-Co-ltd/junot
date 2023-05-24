package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.DistributionShipmentType;

/**
 * 配分出荷指示区分の型変換.
 */
@Converter
public class DistributionShipmentTypeConverter implements AttributeConverter<DistributionShipmentType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final DistributionShipmentType attribute) {
        return DistributionShipmentType.convertToValue(attribute);
    }

    @Override
    public DistributionShipmentType convertToEntityAttribute(final Integer dbData) {
        return DistributionShipmentType.convertToType(dbData);
    }

}
