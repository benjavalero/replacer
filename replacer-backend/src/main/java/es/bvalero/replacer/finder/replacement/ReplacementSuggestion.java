package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.finder.util.FinderUtils;
import lombok.Value;
import org.springframework.lang.Nullable;

@Value(staticConstructor = "of")
public class ReplacementSuggestion {

    String text;

    @Nullable
    String comment;

    public static ReplacementSuggestion ofNoComment(String text) {
        return of(text, null);
    }

    public ReplacementSuggestion toUppercase() {
        return of(FinderUtils.setFirstUpperCase(text), comment);
    }
}
