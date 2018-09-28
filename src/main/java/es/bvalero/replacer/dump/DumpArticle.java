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

    DumpArticle(Integer id, String title, WikipediaNamespace namespace, Date timestamp, String content) {
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

}
