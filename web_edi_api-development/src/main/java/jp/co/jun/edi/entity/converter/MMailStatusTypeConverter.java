package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.MMailStatusType;

/**
 * メールのステータスの型変換.
 */
@Converter
public class MMailStatusTypeConverter implements AttributeConverter<MMailStatusType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final MMailStatusType attribute) {
        return MMailStatusType.convertToValue(attribute);
    }

    @Override
    public MMailStatusType convertToEntityAttribute(final Integer dbData) {
        return MMailStatusType.convertToType(dbData);
    }

}
