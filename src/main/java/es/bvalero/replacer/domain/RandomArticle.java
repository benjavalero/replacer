package es.bvalero.replacer.domain;

import java.io.Serializable;
import java.util.Map;

public class RandomArticle implements Serializable {

    private String title;
    private String content;
    private Map<Integer, Replacement> fixes;

    public RandomArticle() {
    }

    public RandomArticle(String title, String content, Map<Integer, Replacement> fixes) {
        this.title = title;
        this.content = content;
        this.setFixes(fixes);
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

    public Map<Integer, Replacement> getFixes() {
        return fixes;
    }

    public void setFixes(Map<Integer, Replacement> fixes) {
        this.fixes = fixes;
    }

}
