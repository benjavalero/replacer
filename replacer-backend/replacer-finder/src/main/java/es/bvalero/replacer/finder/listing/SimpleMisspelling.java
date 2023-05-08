package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.domain.ReplacementKind;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;

public class SimpleMisspelling extends StandardMisspelling {

    private SimpleMisspelling(String word, boolean caseSensitive, String comment) {
        super(word, caseSensitive, comment);
    }

    @Override
    public void validateMisspellingWord(String word) {
        boolean isValid = StringUtils.isAlpha(word);
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
    public ReplacementKind getReplacementKind() {
        return ReplacementKind.SIMPLE;
    }
}
