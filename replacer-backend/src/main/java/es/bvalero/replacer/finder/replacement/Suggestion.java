package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.finder.util.FinderUtils;
import lombok.Value;
import org.springframework.lang.Nullable;

/**
 * A suggestion for a replacement.
 */
@Value(staticConstructor = "of")
public class Suggestion {

    /** The new text after the replacement */
    String text;

    /** An optional description to explain the motivation of the fix */
    @Nullable
    String comment;

    public static Suggestion ofNoComment(String text) {
        return of(text, null);
    }

    Suggestion toUppercase() {
        return of(FinderUtils.setFirstUpperCase(text), comment);
    }
}
