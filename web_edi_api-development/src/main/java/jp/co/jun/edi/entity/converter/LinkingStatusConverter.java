package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.LinkingStatusType;

/**
 * 連携ステータスの型変換.
 */
@Converter
public class LinkingStatusConverter implements AttributeConverter<LinkingStatusType, String> {

    @Override
    public String convertToDatabaseColumn(final LinkingStatusType attribute) {
        return LinkingStatusType.convertToValue(attribute);
    }

    @Override
    public LinkingStatusType convertToEntityAttribute(final String dbData) {
        return LinkingStatusType.convertToType(dbData);
    }
}
