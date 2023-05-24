package jp.co.jun.edi.component.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.type.LinkingStatusType;

/**
 * リクエストパラメーター用の連携ステータスコンバータークラス.
 */
@Component
public class LinkingStatusTypeConverterComponent implements Converter<String, LinkingStatusType> {
    @Override
    public LinkingStatusType convert(final String source) {
      return LinkingStatusType.convertToType(source);
    }
}
