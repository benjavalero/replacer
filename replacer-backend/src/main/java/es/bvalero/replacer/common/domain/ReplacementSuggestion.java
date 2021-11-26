package es.bvalero.replacer.common.domain;

import lombok.NonNull;
import lombok.Value;
import org.springframework.lang.Nullable;

/** Domain object representing the suggestion for a replacement found in the content of a page */
@Value(staticConstructor = "of")
public class ReplacementSuggestion {

    @NonNull
    String text;

    @Nullable
    String comment;
}
