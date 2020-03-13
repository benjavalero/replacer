package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.article.IndexableArticle;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Value
@Builder
public class WikipediaPage implements IndexableArticle {
    private static final String WIKIPEDIA_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final DateTimeFormatter WIKIPEDIA_DATE_FORMATTER = DateTimeFormatter.ofPattern(WIKIPEDIA_DATE_PATTERN);

    private int id;
    private String title;
    private WikipediaNamespace namespace;
    private LocalDate lastUpdate;
    private String content;
    @With
    private Integer section;

    // Store the timestamp when the page was queried. No need to convert it to Date format.
    private final String queryTimestamp;

    public static LocalDate parseWikipediaTimestamp(String timestamp) {
        return LocalDate.from(WIKIPEDIA_DATE_FORMATTER.parse(timestamp));
    }

    static String formatWikipediaTimestamp(LocalDateTime localDateTime) {
        return WIKIPEDIA_DATE_FORMATTER.format(localDateTime);
    }

}
