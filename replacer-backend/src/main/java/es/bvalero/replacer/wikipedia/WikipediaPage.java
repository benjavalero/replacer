package es.bvalero.replacer.wikipedia;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

final public class WikipediaPage {
    private static final String REDIRECT_PREFIX = "#redirec";

    private final int id;
    private final String title;
    private final WikipediaNamespace namespace;
    private final LocalDate timestamp;
    private final String content;

    private WikipediaPage(int id, String title, WikipediaNamespace namespace, LocalDate timestamp, String content) {
        this.id = id;
        this.title = title;
        this.namespace = namespace;
        this.timestamp = timestamp;
        this.content = content;
    }

    public static WikipediaPage.WikipediaPageBuilder builder() {
        return new WikipediaPage.WikipediaPageBuilder();
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

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public boolean isRedirectionPage() {
        return this.content.toLowerCase().contains(REDIRECT_PREFIX);
    }

    public static class WikipediaPageBuilder {
        private static final String WIKIPEDIA_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

        private int id;
        private String title;
        private WikipediaNamespace namespace;
        private LocalDate timestamp;
        private String content;

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
            this.timestamp = parseWikipediaDate(timestamp);
            return this;
        }

        public WikipediaPage.WikipediaPageBuilder setTimestamp(LocalDate timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public WikipediaPage.WikipediaPageBuilder setContent(String content) {
            this.content = content;
            return this;
        }

        public WikipediaPage build() {
            return new WikipediaPage(id, title, namespace, timestamp, content);
        }

        LocalDate parseWikipediaDate(CharSequence dateStr) {
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(WIKIPEDIA_DATE_PATTERN);
            return LocalDate.from(dateFormat.parse(dateStr));
        }

    }

}
