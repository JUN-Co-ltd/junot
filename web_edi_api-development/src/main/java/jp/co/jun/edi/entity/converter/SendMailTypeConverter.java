package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.SendMailType;

/**
 * メール送信状態の型変換.
 */
@Converter
public class SendMailTypeConverter implements AttributeConverter<SendMailType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final SendMailType attribute) {
        return SendMailType.convertToValue(attribute);
    }

    @Override
    public SendMailType convertToEntityAttribute(final Integer dbData) {
        return SendMailType.convertToType(dbData);
    }

}
