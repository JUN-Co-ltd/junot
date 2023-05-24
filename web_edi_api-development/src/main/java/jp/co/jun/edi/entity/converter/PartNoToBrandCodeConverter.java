package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 品番からブランドコードを表示するための型変換.
 */
@Converter
public class PartNoToBrandCodeConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(final String attribute) {
        return attribute;
    }

    @Override
    public String convertToEntityAttribute(final String dbData) {
        return dbData.substring(0, 2);
    }
}
