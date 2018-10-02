package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;

import java.util.Date;

/**
 * Domain class corresponding to a Wikipedia article in the XML dump.
 */
final class DumpArticle {

    private final Integer id;
    private final String title;
    private final WikipediaNamespace namespace;
    private final Date timestamp;
    private final String content;

    private DumpArticle(Integer id, String title, WikipediaNamespace namespace, Date timestamp, String content) {
        this.id = id;
        this.title = title;
        this.namespace = namespace;
        this.timestamp = timestamp;
        this.content = content;
    }

    static DumpArticle.DumpArticleBuilder builder() {
        return new DumpArticle.DumpArticleBuilder();
    }

    Integer getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    WikipediaNamespace getNamespace() {
        return namespace;
    }

    Date getTimestamp() {
        return timestamp;
    }

    String getContent() {
        return content;
    }

    static class DumpArticleBuilder {
        private Integer id;
        private String title;
        private WikipediaNamespace namespace;
        private Date timestamp;
        private String content;

        DumpArticle.DumpArticleBuilder setId(Integer id) {
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

        DumpArticle.DumpArticleBuilder setTimestamp(Date timestamp) {
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
