package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import org.threeten.bp.LocalDate;

/**
 * Domain class corresponding to a Wikipedia article in the XML dump.
 */
final class DumpArticle {

    private final int id;
    private final String title;
    private final WikipediaNamespace namespace;
    private final LocalDate timestamp;
    private final String content;

    private DumpArticle(int id, String title, WikipediaNamespace namespace, LocalDate timestamp, String content) {
        this.id = id;
        this.title = title;
        this.namespace = namespace;
        this.timestamp = timestamp;
        this.content = content;
    }

    static DumpArticle.DumpArticleBuilder builder() {
        return new DumpArticle.DumpArticleBuilder();
    }

    int getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    WikipediaNamespace getNamespace() {
        return namespace;
    }

    LocalDate getTimestamp() {
        return timestamp;
    }

    String getContent() {
        return content;
    }

    static class DumpArticleBuilder {
        private int id;
        private String title;
        private WikipediaNamespace namespace;
        private LocalDate timestamp;
        private String content;

        DumpArticle.DumpArticleBuilder setId(int id) {
            this.id = id;
            return this;
        }

        DumpArticle.DumpArticleBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        DumpArticle.DumpArticleBuilder setNamespace(WikipediaNamespace namespace) {
            this.namespace = namespace;
            return this;
        }

        DumpArticle.DumpArticleBuilder setTimestamp(LocalDate timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        DumpArticle.DumpArticleBuilder setContent(String content) {
            this.content = content;
            return this;
        }

        DumpArticle build() {
            return new DumpArticle(id, title, namespace, timestamp, content);
        }
    }

}
