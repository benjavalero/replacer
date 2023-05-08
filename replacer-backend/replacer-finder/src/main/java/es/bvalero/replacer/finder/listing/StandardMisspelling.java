package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.finder.util.FinderUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class StandardMisspelling extends Misspelling implements ListingItem {

    StandardMisspelling(String word, boolean caseSensitive, String comment) {
        super(word, caseSensitive, comment);
        validateMisspellingWord(word);
        validateWordCase();
    }

    void validateMisspellingWord(String word) {
        // Throw an exception if anything is wrong
    }

    private void validateWordCase() {
        if (!this.isCaseSensitive() && FinderUtils.startsWithUpperCase(this.getWord())) {
            LOGGER.warn("Case-insensitive uppercase misspelling: " + this.getWord());
        }
    }

    @Override
    public String getKey() {
        return this.getWord();
    }

    public abstract ReplacementKind getReplacementKind();
}
