package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 発注承認ステータスの型変換.
 */
@Converter
public class MCodmstTblIdTypeConverter implements AttributeConverter<MCodmstTblIdType, String> {

    @Override
    public String convertToDatabaseColumn(final MCodmstTblIdType attribute) {
        return MCodmstTblIdType.convertToValue(attribute);
    }

    @Override
    public MCodmstTblIdType convertToEntityAttribute(final String dbData) {
        return MCodmstTblIdType.convertToType(dbData);
    }

}
