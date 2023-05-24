package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.FukukitaruMasterOrderType;

/**
 * フクキタル発注種別の型変換.
 */
@Converter
public class FukukitaruMasterOrderTypeConverter implements AttributeConverter<FukukitaruMasterOrderType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final FukukitaruMasterOrderType attribute) {
        return FukukitaruMasterOrderType.convertToValue(attribute);
    }

    @Override
    public FukukitaruMasterOrderType convertToEntityAttribute(final Integer dbData) {
        return FukukitaruMasterOrderType.convertToType(dbData);
    }

}
