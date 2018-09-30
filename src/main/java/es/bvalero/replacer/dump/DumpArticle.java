package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;

import java.util.Date;

/**
 * Domain class corresponding to a Wikipedia article in the XML dump.
 */
class DumpArticle {

    private Integer id;
    private String title;
    private WikipediaNamespace namespace;
    private Date timestamp;
    private String content;

    @SuppressWarnings("unused")
    private DumpArticle() {
    }

    private DumpArticle(Integer id, String title, WikipediaNamespace namespace, Date timestamp, String content) {
        this.id = id;
        this.title = title;
        this.namespace = namespace;
        this.timestamp = timestamp;
        this.content = content;
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

        DumpArticleBuilder setId(Integer id) {
            this.id = id;
            return this;
        }

        DumpArticleBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        DumpArticleBuilder setNamespace(WikipediaNamespace namespace) {
            this.namespace = namespace;
            return this;
        }

        DumpArticleBuilder setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        DumpArticleBuilder setContent(String content) {
            this.content = content;
            return this;
        }

        DumpArticle createDumpArticle() {
            return new DumpArticle(id, title, namespace, timestamp, content);
        }
    }

}
