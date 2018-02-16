package es.bvalero.replacer.dump;

import java.util.Date;

/**
 * Domain class corresponding to a Wikipedia article in the XML dump.
 */
class DumpArticle {

    private Integer id;
    private String title;
    private Integer namespace;
    private Date timestamp;
    private String content;

    Integer getId() {
        return id;
    }

    void setId(Integer id) {
        this.id = id;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    Integer getNamespace() {
        return namespace;
    }

    void setNamespace(Integer namespace) {
        this.namespace = namespace;
    }

    Date getTimestamp() {
        return timestamp;
    }

    void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    String getContent() {
        return content;
    }

    void setContent(String content) {
        this.content = content;
    }

}
