package es.bvalero.replacer.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
public class ReplacerUtils {

    public final Locale LOCALE_ES = Locale.forLanguageTag("es");

    public String getContextAroundWord(String text, int start, int end, int threshold) {
        int limitLeft = Math.max(0, start - threshold);
        int limitRight = Math.min(text.length(), end + threshold);
        return text.substring(limitLeft, limitRight);
    }

    @Nullable
    public Long convertLocalDateTimeToMilliseconds(@Nullable LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }
}
