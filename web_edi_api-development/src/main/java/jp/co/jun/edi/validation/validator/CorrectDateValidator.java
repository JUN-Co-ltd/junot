package jp.co.jun.edi.validation.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import jp.co.jun.edi.validation.constraints.CorrectDate;

/**
 * 日付の妥当性チェック.
 */
public class CorrectDateValidator implements ConstraintValidator<CorrectDate, String> {
    private static final Pattern PATTERN = Pattern.compile("^[0-9]{4}/[0-9]{1,2}/[0-9]{1,2}$");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu/M/d")
            // 日付を厳密にチェック
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        if (!PATTERN.matcher(value).find()) {
            return false;
        }

        try {
            LocalDate.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return false;
        }

        return true;
    }
}
