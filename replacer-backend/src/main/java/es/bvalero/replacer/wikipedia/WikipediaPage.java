package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.page.IndexablePage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.lang.Nullable;

@Value
@Builder
public class WikipediaPage implements IndexablePage {

    // TODO: Not make public. Use IndexablePage outside this package when needed.

    private static final String WIKIPEDIA_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final DateTimeFormatter WIKIPEDIA_DATE_FORMATTER = DateTimeFormatter.ofPattern(
        WIKIPEDIA_DATE_PATTERN
    );

    int id;
    WikipediaLanguage lang;
    String title;
    WikipediaNamespace namespace;
    LocalDate lastUpdate;
    String content;

    @With(AccessLevel.PACKAGE)
    @Nullable
    Integer section;

    @With(AccessLevel.PACKAGE)
    @Nullable
    String anchor;

    // Store the timestamp when the page was queried. No need to convert it to Date format.
    String queryTimestamp;

    // TODO: Move this two methods to IndexablePage or a helper class
    public static LocalDate parseWikipediaTimestamp(String timestamp) {
        return LocalDate.from(WIKIPEDIA_DATE_FORMATTER.parse(timestamp));
    }

    static String formatWikipediaTimestamp(LocalDateTime localDateTime) {
        return WIKIPEDIA_DATE_FORMATTER.format(localDateTime);
    }

    @Override
    public String toString() {
        return "WikipediaPage{" + "id=" + id + ", lang=" + lang.getCode() + ", title='" + title + '\'' + '}';
    }
}
