package es.bvalero.replacer.wikipedia;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Value
@Builder
public class WikipediaPage {
    private static final String WIKIPEDIA_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final DateTimeFormatter WIKIPEDIA_DATE_FORMATTER = DateTimeFormatter.ofPattern(WIKIPEDIA_DATE_PATTERN);
    private static final String REDIRECT_PREFIX = "#redirec";

    private int id;
    private String title;
    private WikipediaNamespace namespace;
    private LocalDateTime lastUpdate;
    private String content;

    // Store the timestamp when the page was queried
    private final String queryTimestamp;

    public static WikipediaPage.WikipediaPageBuilder builder() {
        return new WikipediaPage.WikipediaPageBuilder();
    }

    public static LocalDateTime parseWikipediaTimestamp(String timestamp) {
        return LocalDateTime.from(WIKIPEDIA_DATE_FORMATTER.parse(timestamp));
    }

    public static String formatWikipediaTimestamp(LocalDateTime localDateTime) {
        return WIKIPEDIA_DATE_FORMATTER.format(localDateTime);
    }

    public boolean isRedirectionPage() {
        return this.content.toLowerCase().contains(REDIRECT_PREFIX);
    }

}
