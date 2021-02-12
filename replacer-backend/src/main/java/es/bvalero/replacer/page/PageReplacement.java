package es.bvalero.replacer.page;

import es.bvalero.replacer.finder.replacement.Suggestion;
import java.util.List;
import lombok.*;

@Value(staticConstructor = "of")
class PageReplacement {

    @With
    int start;

    String text;
    List<Suggestion> suggestions;

    int getEnd() {
        return this.start + this.text.length();
    }
}
