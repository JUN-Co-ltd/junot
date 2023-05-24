package jp.co.jun.edi.entity.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jp.co.jun.edi.type.FukukitaruMasterConfirmStatusType;

/**
 * フクキタル確定ステータスの型変換.
 */
@Converter
public class FukukitaruMasterConfirmStatusTypeConverter implements AttributeConverter<FukukitaruMasterConfirmStatusType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final FukukitaruMasterConfirmStatusType attribute) {
        return FukukitaruMasterConfirmStatusType.convertToValue(attribute);
    }

    @Override
    public FukukitaruMasterConfirmStatusType convertToEntityAttribute(final Integer dbData) {
        return FukukitaruMasterConfirmStatusType.convertToType(dbData);
    }

}
