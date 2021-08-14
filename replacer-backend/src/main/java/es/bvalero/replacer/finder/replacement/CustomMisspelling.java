package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.MisspellingSuggestion;
import java.util.List;
import lombok.Value;

@Value
class CustomMisspelling implements Misspelling {

    String word;
    boolean caseSensitive;
    List<MisspellingSuggestion> suggestions;

    private CustomMisspelling(String word, boolean caseSensitive, String comment) {
        this.word = word;
        this.caseSensitive = caseSensitive;
        this.suggestions = parseComment(comment);
    }

    public static CustomMisspelling of(String word, boolean caseSensitive, String comment) {
        return new CustomMisspelling(word, caseSensitive, comment);
    }

    @Override
    public String getKey() {
        return this.word;
    }

    @Override
    public String getType() {
        return ReplacementType.CUSTOM;
    }
}
