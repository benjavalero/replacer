package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Suggestion;
import lombok.Data;

import java.util.List;

@Data
class ArticleReplacement {
    private int start;
    private String text;
    private List<Suggestion> suggestions;
}
