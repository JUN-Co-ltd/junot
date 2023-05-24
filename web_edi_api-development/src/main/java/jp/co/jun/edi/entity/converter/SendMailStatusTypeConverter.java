package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.SendMailStatusType;

/**
 * メール送信状態の型変換.
 */
@Converter
public class SendMailStatusTypeConverter implements AttributeConverter<SendMailStatusType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final SendMailStatusType attribute) {
        return SendMailStatusType.convertToValue(attribute);
    }

    @Override
    public SendMailStatusType convertToEntityAttribute(final Integer dbData) {
        return SendMailStatusType.convertToType(dbData);
    }

}
