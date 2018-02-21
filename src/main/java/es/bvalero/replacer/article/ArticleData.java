package es.bvalero.replacer.article;

import java.util.Map;

public class ArticleData {

    private Integer id;
    private String title;
    private String content;
    private Map<Integer, ArticleReplacement> fixes;

    ArticleData() {
    }

    ArticleData(Integer id, String title, String content, Map<Integer, ArticleReplacement> fixes) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.fixes = fixes;
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

    @SuppressWarnings("unused")
    public void setFixes(Map<Integer, ArticleReplacement> fixes) {
        this.fixes = fixes;
    }

}
