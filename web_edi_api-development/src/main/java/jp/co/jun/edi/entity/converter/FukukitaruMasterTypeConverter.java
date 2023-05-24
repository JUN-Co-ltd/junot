package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.FukukitaruMasterType;

/**
 * ブランドマスタ種別の型変換.
 */
@Converter
public class FukukitaruMasterTypeConverter implements AttributeConverter<FukukitaruMasterType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final FukukitaruMasterType attribute) {
        return FukukitaruMasterType.convertToValue(attribute);
    }

    @Override
    public FukukitaruMasterType convertToEntityAttribute(final Integer dbData) {
        return FukukitaruMasterType.convertToType(dbData);
    }

}
