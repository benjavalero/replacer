package es.bvalero.replacer.finder.listing;

import lombok.Value;
import org.springframework.lang.Nullable;

@Value(staticConstructor = "of")
public class MisspellingSuggestion {

    String text;

    @Nullable
    String comment;

    static MisspellingSuggestion ofNoComment(String text) {
        return of(text, null);
    }
}
