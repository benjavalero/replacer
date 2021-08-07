package es.bvalero.replacer.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    private static final String WIKIPEDIA_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final DateTimeFormatter WIKIPEDIA_DATE_FORMATTER = DateTimeFormatter.ofPattern(
        WIKIPEDIA_DATE_PATTERN
    );

    public static LocalDateTime parseWikipediaTimestamp(String timestamp) {
        return LocalDateTime.from(WIKIPEDIA_DATE_FORMATTER.parse(timestamp));
    }

    public static String formatWikipediaTimestamp(LocalDateTime localDateTime) {
        return WIKIPEDIA_DATE_FORMATTER.format(localDateTime);
    }
}
