package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.util.FinderUtils;
import org.jetbrains.annotations.TestOnly;

public class SimpleMisspelling extends StandardMisspelling {

    private SimpleMisspelling(String word, boolean caseSensitive, String comment) {
        super(word, caseSensitive, comment);
    }

    @Override
    public void validateMisspellingWord(String word) {
        if (!isValidMisspellingWord(word)) {
            throw new IllegalArgumentException("Not valid misspelling word: " + word);
        }
    }

    private boolean isValidMisspellingWord(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (!FinderUtils.isLetter(word.charAt(i))) {
                return false;
            }
        }
        return true;
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
    public ReplacementKind getReplacementKind() {
        return ReplacementKind.SIMPLE;
    }
}
