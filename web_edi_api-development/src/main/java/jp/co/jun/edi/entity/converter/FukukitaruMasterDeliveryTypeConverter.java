package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;

/**
 * フクキタルデリバリ種別の型変換.
 */
@Converter
public class FukukitaruMasterDeliveryTypeConverter implements AttributeConverter<FukukitaruMasterDeliveryType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final FukukitaruMasterDeliveryType attribute) {
        return FukukitaruMasterDeliveryType.convertToValue(attribute);
    }

    @Override
    public FukukitaruMasterDeliveryType convertToEntityAttribute(final Integer dbData) {
        return FukukitaruMasterDeliveryType.convertToType(dbData);
    }

}
