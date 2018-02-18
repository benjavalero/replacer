package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Domain class corresponding to a Wikipedia article in the XML dump.
 */
class DumpArticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpArticle.class);

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

    boolean isProcessable() {
        if (!WikipediaNamespace.ARTICLE.equals(getNamespace()) && !WikipediaNamespace.ANNEX.equals(getNamespace())) {
            LOGGER.debug("Only articles and annexes are processed. Skipping namespace: {}", getNamespace());
            return false;
        } else if (WikipediaUtils.isRedirectionArticle(getContent())) {
            LOGGER.debug("Redirection article. Skipping.");
            return false;
        } else {
            return true;
        }
    }

}
