package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.DelischeProductionStatusType;

/**
 * デリスケ生産工程区分の型変換.
 */
@Converter
public class DelischeProductionStatusTypeConverter implements AttributeConverter<DelischeProductionStatusType, String> {
    @Override
    public String convertToDatabaseColumn(final DelischeProductionStatusType attribute) {
        return DelischeProductionStatusType.convertToValue(attribute);
    }

    @Override
    public DelischeProductionStatusType convertToEntityAttribute(final String dbData) {
        return DelischeProductionStatusType.convertToType(dbData);
    }
}
