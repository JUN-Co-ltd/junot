package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.OrderApprovalType;

/**
 * 発注承認ステータスの型変換.
 */
@Converter
public class OrderApprovalTypeConverter implements AttributeConverter<OrderApprovalType, String> {

    @Override
    public String convertToDatabaseColumn(final OrderApprovalType attribute) {
        return OrderApprovalType.convertToValue(attribute);
    }

    @Override
    public OrderApprovalType convertToEntityAttribute(final String dbData) {
        return OrderApprovalType.convertToType(dbData);
    }

}
