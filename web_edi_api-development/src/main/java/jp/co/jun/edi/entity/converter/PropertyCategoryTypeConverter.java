package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.PropertyCategoryType;

/**
 * プロパティカテゴリの型変換.
 */
@Converter
public class PropertyCategoryTypeConverter implements AttributeConverter<PropertyCategoryType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final PropertyCategoryType attribute) {
        return PropertyCategoryType.convertToValue(attribute);
    }

    @Override
    public PropertyCategoryType convertToEntityAttribute(final Integer dbData) {
        return PropertyCategoryType.convertToType(dbData);
    }

}
