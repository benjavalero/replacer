package es.bvalero.replacer.finder.listing;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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

    @EqualsAndHashCode.Include
    @Override
    public String getKey() {
        return this.word;
    }
}
