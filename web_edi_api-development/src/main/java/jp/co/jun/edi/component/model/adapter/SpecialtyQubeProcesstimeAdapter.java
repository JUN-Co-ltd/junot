package jp.co.jun.edi.component.model.adapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * SQの処理時間(processtime)の型変換.
 */
public class SpecialtyQubeProcesstimeAdapter extends XmlAdapter<String, LocalDateTime> {

    static final int MILLI_SECOND_DIGITS = 6;

    @Override
    public LocalDateTime unmarshal(final String value) throws Exception {
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .appendFraction(ChronoField.MICRO_OF_SECOND, MILLI_SECOND_DIGITS, MILLI_SECOND_DIGITS, true)
                .toFormatter();

        return LocalDateTime.parse(value, formatter);
    }

    @Override
    public String marshal(final LocalDateTime value) throws Exception {
        return "";  // SQ連携ではprocesstimeのmarshalは使わない
    }

}
