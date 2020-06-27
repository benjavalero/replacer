package es.bvalero.replacer.page;

import es.bvalero.replacer.finder.Suggestion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
class PageReplacement {
    @With
    private int start;
    private String text;
    private List<Suggestion> suggestions;

    int getEnd() {
        return this.start + this.text.length();
    }

}
