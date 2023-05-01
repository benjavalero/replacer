package es.bvalero.replacer.wikipedia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.jetbrains.annotations.VisibleForTesting;

/** A timestamp in Wikipedia, for instance for the last update or the query instant. */
@EqualsAndHashCode
public class WikipediaTimestamp {

    private static final String WIKIPEDIA_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final DateTimeFormatter WIKIPEDIA_DATE_FORMATTER = DateTimeFormatter.ofPattern(
        WIKIPEDIA_DATE_PATTERN
    );

    // Store internally the timestamp as a date
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime localDateTime;

    public static WikipediaTimestamp of(String timestamp) {
        WikipediaTimestamp wikipediaTimestamp = new WikipediaTimestamp();
        wikipediaTimestamp.setLocalDateTime(parseWikipediaTimestamp(timestamp));
        return wikipediaTimestamp;
    }

    public static WikipediaTimestamp now() {
        return of(LocalDateTime.now());
    }

    @VisibleForTesting
    public static WikipediaTimestamp of(LocalDateTime localDateTime) {
        WikipediaTimestamp wikipediaTimestamp = new WikipediaTimestamp();
        wikipediaTimestamp.setLocalDateTime(localDateTime.truncatedTo(ChronoUnit.SECONDS));
        return wikipediaTimestamp;
    }

    public LocalDate toLocalDate() {
        return this.localDateTime.toLocalDate();
    }

    @VisibleForTesting
    LocalDateTime toLocalDateTime() {
        return this.localDateTime;
    }

    private static LocalDateTime parseWikipediaTimestamp(String timestamp) {
        return LocalDateTime.from(WIKIPEDIA_DATE_FORMATTER.parse(timestamp)).truncatedTo(ChronoUnit.SECONDS);
    }

    public boolean isBeforeOrEquals(WikipediaTimestamp timestamp) {
        return !this.toLocalDateTime().isAfter(timestamp.toLocalDateTime());
    }

    @Override
    public String toString() {
        return WIKIPEDIA_DATE_FORMATTER.format(this.localDateTime);
    }
}
