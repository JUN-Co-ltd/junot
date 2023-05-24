package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.QualityApprovalType;

/**
 * 優良誤認承認区分の型変換.
 */
@Converter
public class QualityApprovalTypeConverter implements AttributeConverter<QualityApprovalType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final QualityApprovalType attribute) {
        return QualityApprovalType.convertToValue(attribute);
    }

    @Override
    public QualityApprovalType convertToEntityAttribute(final Integer dbData) {
        return QualityApprovalType.convertToType(dbData);
    }

}
