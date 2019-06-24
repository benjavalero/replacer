package es.bvalero.replacer.wikipedia;

import org.jetbrains.annotations.TestOnly;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class WikipediaPage {
    private static final String WIKIPEDIA_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final DateTimeFormatter WIKIPEDIA_DATE_FORMATTER = DateTimeFormatter.ofPattern(WIKIPEDIA_DATE_PATTERN);
    private static final String REDIRECT_PREFIX = "#redirec";

    private final int id;
    private final String title;
    private final WikipediaNamespace namespace;
    private final String timestamp;
    private final String content;

    // Store the timestamp when the page was queried
    private final String queryTimestamp;

    private WikipediaPage(int id, String title, WikipediaNamespace namespace, String timestamp, String content, String queryTimestamp) {
        this.id = id;
        this.title = title;
        this.namespace = namespace;
        this.timestamp = timestamp;
        this.content = content;
        this.queryTimestamp = queryTimestamp;
    }

    public static WikipediaPage.WikipediaPageBuilder builder() {
        return new WikipediaPage.WikipediaPageBuilder();
    }

    static LocalDateTime parseWikipediaTimestamp(String timestamp) {
        return LocalDateTime.from(WIKIPEDIA_DATE_FORMATTER.parse(timestamp));
    }

    public static String formatWikipediaTimestamp(LocalDateTime localDateTime) {
        return WIKIPEDIA_DATE_FORMATTER.format(localDateTime);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public WikipediaNamespace getNamespace() {
        return namespace;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public LocalDateTime getLastUpdate() {
        return parseWikipediaTimestamp(timestamp);
    }

    public String getContent() {
        return content;
    }

    public String getQueryTimestamp() {
        return queryTimestamp;
    }

    public boolean isRedirectionPage() {
        return this.content.toLowerCase().contains(REDIRECT_PREFIX);
    }

    public static class WikipediaPageBuilder {

        private int id;
        private String title;
        private WikipediaNamespace namespace;
        private String timestamp;
        private String content;
        private String queryTimestamp;

        public WikipediaPage.WikipediaPageBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public WikipediaPage.WikipediaPageBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public WikipediaPage.WikipediaPageBuilder setNamespace(int namespace) {
            this.namespace = WikipediaNamespace.valueOf(namespace);
            return this;
        }

        public WikipediaPage.WikipediaPageBuilder setNamespace(WikipediaNamespace namespace) {
            this.namespace = namespace;
            return this;
        }

        public WikipediaPage.WikipediaPageBuilder setTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        @TestOnly
        public WikipediaPage.WikipediaPageBuilder setTimestamp(LocalDateTime timestamp) {
            this.timestamp = formatWikipediaTimestamp(timestamp);
            return this;
        }

        public WikipediaPage.WikipediaPageBuilder setContent(String content) {
            this.content = content;
            return this;
        }

        public WikipediaPage.WikipediaPageBuilder setQueryTimestamp(String queryTimestamp) {
            this.queryTimestamp = queryTimestamp;
            return this;
        }

        public WikipediaPage build() {
            return new WikipediaPage(id, title, namespace, timestamp, content, queryTimestamp);
        }

    }

}
