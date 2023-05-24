package jp.co.jun.edi.entity.converter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang.StringUtils;

/**
 * カンマ区切りの文字列をList<BigInteger>の型変換.
 */
@Converter
public class BigIntegerArrayTypeConverter implements AttributeConverter<List<BigInteger>, String> {
    private static final String COMMA = ",";

    @Override
    public String convertToDatabaseColumn(final List<BigInteger> attribute) {
        return attribute.stream().map(data -> data.toString()).collect(Collectors.joining(COMMA));
    }

    @Override
    public List<BigInteger> convertToEntityAttribute(final String dbData) {
        if (dbData == null) {
            return new ArrayList<BigInteger>();
        }
        return Arrays.asList(StringUtils.split(dbData, COMMA)).stream().map(data -> new BigInteger(data)).collect(Collectors.toList());
    }

}
