package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.domain.ReplacementKind;
import java.util.List;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;

@Value
public class SimpleMisspelling implements Misspelling {

    String word;
    boolean caseSensitive;
    List<MisspellingSuggestion> suggestions;

    private SimpleMisspelling(String word, boolean caseSensitive, String comment) {
        // Validate the word
        validateMisspellingWord(word);

        this.word = word;
        this.caseSensitive = caseSensitive;
        this.suggestions = parseComment(comment);

        validateWordCase();
    }

    private void validateMisspellingWord(String word) {
        boolean isValid = word.chars().allMatch(Character::isLetter);
        if (!isValid) {
            throw new IllegalArgumentException("Not valid misspelling word: " + word);
        }
    }

    public static SimpleMisspelling of(String word, boolean caseSensitive, String comment) {
        return new SimpleMisspelling(word, caseSensitive, comment);
    }

    @TestOnly
    public static SimpleMisspelling ofCaseInsensitive(String word, String comment) {
        return of(word, false, comment);
    }

    @TestOnly
    public static SimpleMisspelling ofCaseSensitive(String word, String comment) {
        return of(word, true, comment);
    }

    @Override
    public String getKey() {
        return this.word;
    }

    @Override
    public ReplacementKind getReplacementKind() {
        return ReplacementKind.SIMPLE;
    }
}
