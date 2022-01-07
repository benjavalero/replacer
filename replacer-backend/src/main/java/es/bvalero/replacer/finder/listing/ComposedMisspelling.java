package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.domain.ReplacementKind;
import java.util.List;
import lombok.Value;

@Value
public class ComposedMisspelling implements Misspelling {

    String word;
    boolean caseSensitive;
    List<MisspellingSuggestion> suggestions;

    private ComposedMisspelling(String word, boolean caseSensitive, String comment) {
        this.word = word;
        this.caseSensitive = caseSensitive;
        this.suggestions = parseComment(comment);

        validateWordCase();
    }

    public static ComposedMisspelling of(String word, boolean caseSensitive, String comment) {
        return new ComposedMisspelling(word, caseSensitive, comment);
    }

    @Override
    public String getKey() {
        return this.word;
    }

    @Override
    public ReplacementKind getReplacementKind() {
        return ReplacementKind.MISSPELLING_COMPOSED;
    }
}
