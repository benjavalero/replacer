package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaUtils;

import java.util.Date;

/**
 * Domain class corresponding to a Wikipedia article in the XML dump.
 */
class DumpArticle {

    private final Integer id;
    private final String title;
    private final WikipediaNamespace namespace;
    private final Date timestamp;
    private final String content;

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

    boolean isProcessable() {
        if (!WikipediaNamespace.ARTICLE.equals(getNamespace()) && !WikipediaNamespace.ANNEX.equals(getNamespace())) {
            return false;
        } else return !WikipediaUtils.isRedirectionArticle(getContent());
    }

}
