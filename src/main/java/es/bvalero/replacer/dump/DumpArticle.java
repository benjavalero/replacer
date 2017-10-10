package es.bvalero.replacer.dump;

import java.util.Date;

class DumpArticle {

    private Integer id;
    private String title;
    private Integer namespace;
    private Date timestamp;
    private String content;

    public DumpArticle(Integer id, String title, Integer namespace, Date timestamp, String content) {
        this.id = id;
        this.title = title;
        this.namespace = namespace;
        this.timestamp = timestamp;
        this.content = content;
    }

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
