package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.DelischeFileStatusType;

/**
 * デリスケファイルステータスの型変換.
 */
@Converter
public class DelischeFileStatusTypeConverter implements AttributeConverter<DelischeFileStatusType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final DelischeFileStatusType attribute) {
        return DelischeFileStatusType.convertToValue(attribute);
    }

    @Override
    public DelischeFileStatusType convertToEntityAttribute(final Integer dbData) {
        return DelischeFileStatusType.convertToType(dbData);
    }

}
