package es.bvalero.replacer.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
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

    public String replaceInText(String text, int start, String current, String replacement) {
        final int end = start + current.length();
        if (!text.substring(start, end).equals(current)) {
            throw new IllegalArgumentException();
        }
        return text.substring(0, start) + replacement + text.substring(end);
    }

    @Nullable
    public Long convertLocalDateTimeToMilliseconds(@Nullable LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    public String toJson(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }

    public String toJson(Object... objs) {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < objs.length; i += 2) {
            map.put(objs[i].toString(), objs[i + 1]);
        }
        return toJson(map);
    }
}
