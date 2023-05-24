package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.ApprovalType;

/**
 * 承認ステータスの型変換.
 */
@Converter
public class ApprovalTypeConverter implements AttributeConverter<ApprovalType, String> {

    @Override
    public String convertToDatabaseColumn(final ApprovalType attribute) {
        return ApprovalType.convertToValue(attribute);
    }

    @Override
    public ApprovalType convertToEntityAttribute(final String dbData) {
        return ApprovalType.convertToType(dbData);
    }
}
