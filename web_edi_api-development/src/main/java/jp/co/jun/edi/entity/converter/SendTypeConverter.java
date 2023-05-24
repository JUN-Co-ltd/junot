package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.SendType;

/**
 * 済区分の型変換.
 */
@Converter
public class SendTypeConverter implements AttributeConverter<SendType, String> {

    @Override
    public String convertToDatabaseColumn(final SendType attribute) {
        return SendType.convertToValue(attribute);
    }

    @Override
    public SendType convertToEntityAttribute(final String dbData) {
        return SendType.convertToType(dbData);
    }
}
