package es.bvalero.replacer.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.JsonMapperConfiguration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@UtilityClass
public class ReplacerUtils {

    public final Locale LOCALE_ES = Locale.forLanguageTag("es");

    //region String Utils

    public String getContextAroundWord(String text, int start, int end, int threshold) {
        int limitLeft = Math.max(0, start - threshold);
        int limitRight = Math.min(text.length(), end + threshold);
        return text.substring(limitLeft, limitRight);
    }

    public String replaceInText(String text, int start, String current, String replacement) {
        final int end = start + current.length();
        final String actual = text.substring(start, end);
        if (!actual.equals(current)) {
            throw new IllegalArgumentException("Replacement mismatch: %s <> %s".formatted(actual, current));
        }
        return text.substring(0, start) + replacement + text.substring(end);
    }

    /**
     * Capitalizes a string changing the first letter appearing in the text to title case,
     * e.g. to capitalize a text enclosed by quotes.
     */
    public String setFirstUpperCaseIgnoringNonLetters(String text) {
        if (StringUtils.isEmpty(text) || Character.isUpperCase(text.charAt(0))) {
            return text;
        }

        if (Character.isLetterOrDigit(text.charAt(0))) {
            return setFirstUpperCase(text);
        }

        // Find the first letter
        int startFirstLetter = -1;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetterOrDigit(text.charAt(i))) {
                startFirstLetter = i;
                break;
            }
        }
        if (startFirstLetter < 0) {
            // No letters in the text
            return text;
        } else {
            return text.substring(0, startFirstLetter) + setFirstUpperCase(text.substring(startFirstLetter));
        }
    }

    /** Capitalizes a string changing the first character of the text to uppercase */
    public String setFirstUpperCase(String text) {
        if (StringUtils.isEmpty(text) || Character.isUpperCase(text.charAt(0))) {
            return text;
        }

        return toUpperCase(text.substring(0, 1)) + text.substring(1);
    }

    /** Converts all the characters in this text to upper case */
    public String toUpperCase(String text) {
        return text.toUpperCase(LOCALE_ES);
    }

    /** Converts all the characters in this text to lower case */
    public String toLowerCase(String text) {
        return text.toLowerCase(LOCALE_ES);
    }

    public String escapeRegexChars(String text) {
        return text
            .replace(".", "\\.")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("&", "\\&");
    }

    //endregion

    //region Date Utils

    @Nullable
    public Long convertLocalDateTimeToMilliseconds(@Nullable LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    //endregion

    //region Logging Utils

    public String toJson(Object obj) {
        ObjectMapper mapper = JsonMapperConfiguration.buildJsonMapper();
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

    //endregion

    //region Logging Utils

    public <T> Stream<T> streamOfIterable(Iterable<T> iterable) {
        // According to benchmarks, it is better to convert a custom iterable into a stream
        // than using the Stream.iterate generator.
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterable.iterator(), Spliterator.ORDERED),
            false
        );
    }
    //endregion
}
