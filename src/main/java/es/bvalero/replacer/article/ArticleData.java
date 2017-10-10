package es.bvalero.replacer.article;

import java.io.Serializable;
import java.util.Map;

public class ArticleData implements Serializable {

    private Integer id;
    private String title;
    private String content;
    private Map<Integer, ArticleReplacement> fixes;

    ArticleData() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<Integer, ArticleReplacement> getFixes() {
        return fixes;
    }

    public void setFixes(Map<Integer, ArticleReplacement> fixes) {
        this.fixes = fixes;
    }

}
