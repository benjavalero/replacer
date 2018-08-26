package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaUtils;

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

    DumpArticle() {
    }

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

    public void setId(Integer id) {
        this.id = id;
    }

    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    WikipediaNamespace getNamespace() {
        return namespace;
    }

    public void setNamespace(WikipediaNamespace namespace) {
        this.namespace = namespace;
    }

    Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    boolean isProcessable() {
        if (!WikipediaNamespace.ARTICLE.equals(getNamespace()) && !WikipediaNamespace.ANNEX.equals(getNamespace())) {
            return false;
        } else return !WikipediaUtils.isRedirectionArticle(getContent());
    }

}
