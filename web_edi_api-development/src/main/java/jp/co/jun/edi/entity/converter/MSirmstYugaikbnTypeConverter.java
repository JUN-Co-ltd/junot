package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.MSirmstYugaikbnType;

/**
 * 有害区分の型変換.
 */
@Converter
public class MSirmstYugaikbnTypeConverter implements AttributeConverter<MSirmstYugaikbnType, String> {

    @Override
    public String convertToDatabaseColumn(final MSirmstYugaikbnType attribute) {
        return MSirmstYugaikbnType.convertToValue(attribute);
    }

    @Override
    public MSirmstYugaikbnType convertToEntityAttribute(final String dbData) {
        return MSirmstYugaikbnType.convertToType(dbData);
    }
}
