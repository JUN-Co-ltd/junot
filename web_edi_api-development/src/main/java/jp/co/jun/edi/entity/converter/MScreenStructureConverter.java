package jp.co.jun.edi.entity.converter;

import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.type.TypeReference;

import jp.co.jun.edi.entity.extended.ExtendedMScreenStructureEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * コードマスタのが画面定義変換.
 */
@Slf4j
@Converter
public class MScreenStructureConverter implements AttributeConverter<List<ExtendedMScreenStructureEntity>, String> {
    private static final TypeReference<List<ExtendedMScreenStructureEntity>> TYPE_REFERENCE
        = new TypeReference<List<ExtendedMScreenStructureEntity>>() {
    };
    @Override
    public String convertToDatabaseColumn(final List<ExtendedMScreenStructureEntity> attribute) {
        try {
            return ObjectMapperUtil.getObjectMapper().writeValueAsString(attribute);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.SYSTEM_ERROR));
        }
    }
    @Override
    public List<ExtendedMScreenStructureEntity> convertToEntityAttribute(final String dbData) {
        try {
            return ObjectMapperUtil.getObjectMapper().readValue(dbData, TYPE_REFERENCE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.SYSTEM_ERROR));
        }
    }
}
