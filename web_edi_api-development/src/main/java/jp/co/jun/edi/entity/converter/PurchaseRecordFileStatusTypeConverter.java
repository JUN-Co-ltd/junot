//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.PurchaseRecordFileStatusType;

/**
 * 仕入実績ファイルステータスの型変換.
 */
@Converter
public class PurchaseRecordFileStatusTypeConverter implements AttributeConverter<PurchaseRecordFileStatusType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final PurchaseRecordFileStatusType attribute) {
        return PurchaseRecordFileStatusType.convertToValue(attribute);
    }

    @Override
    public PurchaseRecordFileStatusType convertToEntityAttribute(final Integer dbData) {
        return PurchaseRecordFileStatusType.convertToType(dbData);
    }

}
//PRD_0133 #10181 add JFE end