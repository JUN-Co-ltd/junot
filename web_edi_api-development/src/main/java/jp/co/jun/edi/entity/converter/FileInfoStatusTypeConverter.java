package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.FileInfoStatusType;

/**
 * ファイル状態の型変換.
 */
@Converter
public class FileInfoStatusTypeConverter implements AttributeConverter<FileInfoStatusType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final FileInfoStatusType attribute) {
        return FileInfoStatusType.convertToValue(attribute);
    }

    @Override
    public FileInfoStatusType convertToEntityAttribute(final Integer dbData) {
        return FileInfoStatusType.convertToType(dbData);
    }

}
