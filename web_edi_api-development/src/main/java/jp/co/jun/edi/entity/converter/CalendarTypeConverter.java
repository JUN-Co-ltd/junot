package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.CalendarType;

/**
 * カレンダー種別の型変換.
 */
@Converter
public class CalendarTypeConverter implements AttributeConverter<CalendarType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final CalendarType attribute) {
        return CalendarType.convertToValue(attribute);
    }

    @Override
    public CalendarType convertToEntityAttribute(final Integer dbData) {
        return CalendarType.convertToType(dbData);
    }

}
