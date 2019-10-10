package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Suggestion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ArticleReplacement {
    @Wither
    private int start;
    private String text;
    private List<Suggestion> suggestions;

    int getEnd() {
        return this.start + this.text.length();
    }

}
