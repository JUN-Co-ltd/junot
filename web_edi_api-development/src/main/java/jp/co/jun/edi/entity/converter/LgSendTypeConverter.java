package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.LgSendType;

/**
 * LG送信区分の型変換.
 */
@Converter
public class LgSendTypeConverter implements AttributeConverter<LgSendType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final LgSendType attribute) {
        return LgSendType.convertToValue(attribute);
    }

    @Override
    public LgSendType convertToEntityAttribute(final Integer dbData) {
        return LgSendType.convertToType(dbData);
    }
}
