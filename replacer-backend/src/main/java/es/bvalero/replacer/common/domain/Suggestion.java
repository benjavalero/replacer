package es.bvalero.replacer.common.domain;

import lombok.Value;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Domain object representing the suggestion for a replacement found in the content of a page */
@Value(staticConstructor = "of")
public class Suggestion {

    @NonNull
    String text;

    @Nullable
    String comment;

    @TestOnly
    public static Suggestion ofNoComment(String text) {
        return of(text, null);
    }
}
