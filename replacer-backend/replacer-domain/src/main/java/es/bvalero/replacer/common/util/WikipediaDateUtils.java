package es.bvalero.replacer.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WikipediaDateUtils {

    private final String WIKIPEDIA_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final DateTimeFormatter WIKIPEDIA_DATE_FORMATTER = DateTimeFormatter.ofPattern(WIKIPEDIA_DATE_PATTERN);

    public LocalDateTime parseWikipediaTimestamp(String timestamp) {
        return LocalDateTime.from(WIKIPEDIA_DATE_FORMATTER.parse(timestamp)).truncatedTo(ChronoUnit.SECONDS);
    }

    public String formatWikipediaTimestamp(LocalDateTime localDateTime) {
        return WIKIPEDIA_DATE_FORMATTER.format(localDateTime);
    }
}
