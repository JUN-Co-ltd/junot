package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.FukukitaruMasterDestinationType;

/**
 * フクキタル宛先種別の型変換.
 */
@Converter
public class FukukitaruMasterAddressTypeConverter implements AttributeConverter<FukukitaruMasterDestinationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final FukukitaruMasterDestinationType attribute) {
        return FukukitaruMasterDestinationType.convertToValue(attribute);
    }

    @Override
    public FukukitaruMasterDestinationType convertToEntityAttribute(final Integer dbData) {
        return FukukitaruMasterDestinationType.convertToType(dbData);
    }

}
