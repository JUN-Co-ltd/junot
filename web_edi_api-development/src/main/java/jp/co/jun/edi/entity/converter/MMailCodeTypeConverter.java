package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.MMailCodeType;

/**
 * メール分類の型変換.
 */
@Converter
public class MMailCodeTypeConverter implements AttributeConverter<MMailCodeType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final MMailCodeType attribute) {
        return MMailCodeType.convertToValue(attribute);
    }

    @Override
    public MMailCodeType convertToEntityAttribute(final Integer dbData) {
        return MMailCodeType.convertToType(dbData);
    }

}
