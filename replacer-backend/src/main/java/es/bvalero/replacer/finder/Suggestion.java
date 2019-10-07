package es.bvalero.replacer.finder;

import lombok.Value;

@Value(staticConstructor = "of")
public class Suggestion {
    private String text;
    private String comment;

    public static Suggestion ofNoComment(String text) {
        return of(text, null);
    }

}
