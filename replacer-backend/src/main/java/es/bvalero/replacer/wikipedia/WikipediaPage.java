package es.bvalero.replacer.wikipedia;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Value(staticConstructor = "of")
@Builder
public class WikipediaPage {
    private static final String WIKIPEDIA_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final DateTimeFormatter WIKIPEDIA_DATE_FORMATTER = DateTimeFormatter.ofPattern(WIKIPEDIA_DATE_PATTERN);
    private static final String PREFIX_REDIRECT = "#redirec";
    private static final String TEMPLATE_DESTROY = "{{destruir";
    private static final String TEMPLATE_COPY_EDIT = "{{copyedit";
    private static final List<String> TEMPLATES_NOT_PROCESSABLE =
            Arrays.asList(PREFIX_REDIRECT, TEMPLATE_DESTROY, TEMPLATE_COPY_EDIT);

    @Wither
    private int id;
    private String title;
    private WikipediaNamespace namespace;
    private LocalDateTime lastUpdate;
    private String content;
    @Wither
    private Integer section;

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

    public boolean isProcessableByContent() {
        String lowerContent = this.content.toLowerCase();
        return TEMPLATES_NOT_PROCESSABLE.stream().noneMatch(lowerContent::contains);
    }

}
