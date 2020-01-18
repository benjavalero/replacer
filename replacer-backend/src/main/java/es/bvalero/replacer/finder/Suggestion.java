package es.bvalero.replacer.finder;

import lombok.Value;

/**
 * A suggestion for a replacement.
 */
@Value(staticConstructor = "of")
public class Suggestion {
    /** The new text after the replacement */
    private String text;

    /** An optional description to explain the motivation of the fix */
    private String comment;

    public static Suggestion ofNoComment(String text) {
        return of(text, null);
    }
}
