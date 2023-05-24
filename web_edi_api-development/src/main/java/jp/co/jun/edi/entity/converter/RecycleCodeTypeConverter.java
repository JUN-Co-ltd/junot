package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.FukukitaruMasterLinkingStatusType;

/**
 * フクキタル連携ステータスの型変換.
 */
@Converter
public class RecycleCodeTypeConverter implements AttributeConverter<FukukitaruMasterLinkingStatusType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final FukukitaruMasterLinkingStatusType attribute) {
        return FukukitaruMasterLinkingStatusType.convertToValue(attribute);
    }

    @Override
    public FukukitaruMasterLinkingStatusType convertToEntityAttribute(final Integer dbData) {
        return FukukitaruMasterLinkingStatusType.convertToType(dbData);
    }

}
