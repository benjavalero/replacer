package es.bvalero.replacer.common.domain;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Suggestion for a replacement found in the content of a page */
@Value(staticConstructor = "of")
public class Suggestion {

    private static final String NO_REPLACE = "no reemplazar";

    @NonNull
    String text;

    @Nullable
    String comment;

    public Suggestion(String text, @Nullable String comment) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Blank suggestion text");
        }

        this.text = text;
        this.comment = comment;
    }

    @TestOnly
    public static Suggestion ofNoComment(String text) {
        return of(text, null);
    }

    public static Suggestion ofNoReplace(String text) {
        return of(text, NO_REPLACE);
    }

    public Suggestion merge(Suggestion suggestion) {
        if (!this.getText().equals(suggestion.getText())) {
            throw new IllegalArgumentException();
        }

        String mergedComment = String.format("%s; %s", this.comment, suggestion.getComment());
        return Suggestion.of(this.text, mergedComment);
    }
}
