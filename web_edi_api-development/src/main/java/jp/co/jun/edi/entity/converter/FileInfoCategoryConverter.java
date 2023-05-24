package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.FileInfoCategory;

/**
 * 有効ステータスの型変換.
 */
@Converter
public class FileInfoCategoryConverter implements AttributeConverter<FileInfoCategory, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final FileInfoCategory attribute) {
        return FileInfoCategory.convertToValue(attribute);
    }

    @Override
    public FileInfoCategory convertToEntityAttribute(final Integer dbData) {
        return FileInfoCategory.convertToType(dbData);
    }

}
