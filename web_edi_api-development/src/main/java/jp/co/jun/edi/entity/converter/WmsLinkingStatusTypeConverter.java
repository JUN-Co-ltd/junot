package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.WmsLinkingStatusType;

/**
 * WMS連携ステータスの型変換.
 */
@Converter
public class WmsLinkingStatusTypeConverter implements AttributeConverter<WmsLinkingStatusType, String> {

    @Override
    public String convertToDatabaseColumn(final WmsLinkingStatusType attribute) {
        return WmsLinkingStatusType.convertToValue(attribute);
    }

    @Override
    public WmsLinkingStatusType convertToEntityAttribute(final String dbData) {
        return WmsLinkingStatusType.convertToType(dbData);
    }
}
