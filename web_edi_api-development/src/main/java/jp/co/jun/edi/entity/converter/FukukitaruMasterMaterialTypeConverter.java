package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.FukukitaruMasterMaterialType;

/**
 * フクキタル資材種別の型変換.
 */
@Converter
public class FukukitaruMasterMaterialTypeConverter implements AttributeConverter<FukukitaruMasterMaterialType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final FukukitaruMasterMaterialType attribute) {
        return FukukitaruMasterMaterialType.convertToValue(attribute);
    }

    @Override
    public FukukitaruMasterMaterialType convertToEntityAttribute(final Integer dbData) {
        return FukukitaruMasterMaterialType.convertToType(dbData);
    }

}
