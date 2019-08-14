package es.bvalero.replacer.finder;

import lombok.Value;

@Value(staticConstructor = "of")
public class ReplacementSuggestion {
    private String text;
    private String comment;

    public static ReplacementSuggestion ofNoComment(String text) {
        return of(text, null);
    }

}
