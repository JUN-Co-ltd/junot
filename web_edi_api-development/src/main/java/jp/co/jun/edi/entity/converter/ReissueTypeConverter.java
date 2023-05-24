package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.ReissueType;

/**
 * 再発行フラグの型変換.
 */
@Converter
public class ReissueTypeConverter implements AttributeConverter<ReissueType, String> {

    @Override
    public String convertToDatabaseColumn(final ReissueType attribute) {
        return ReissueType.convertToValue(attribute);
    }

    @Override
    public ReissueType convertToEntityAttribute(final String dbData) {
        return ReissueType.convertToType(dbData);
    }
}
