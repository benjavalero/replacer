package es.bvalero.replacer.finder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;

/**
 * Domain class of a potential replacement in an article.
 */
@Value(staticConstructor = "of")
@Builder
public class ArticleReplacement implements Comparable<ArticleReplacement> {

    @JsonIgnore
    private String type;
    @JsonIgnore
    private String subtype;

    @Wither
    private int start;
    private String text;
    private List<ReplacementSuggestion> suggestions;

    @JsonIgnore
    public int getEnd() {
        return this.start + this.text.length();
    }

    @Override
    public int compareTo(ArticleReplacement o) {
        // Order descendant by start. If equals, the lower end.
        return o.start == start ? getEnd() - o.getEnd() : o.start - start;
    }

}
