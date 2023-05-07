package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.domain.ReplacementKind;

public class ComposedMisspelling extends Misspelling implements ListingItem {

    private ComposedMisspelling(String word, boolean caseSensitive, String comment) {
        super(word, caseSensitive, comment);
    }

    public static ComposedMisspelling of(String word, boolean caseSensitive, String comment) {
        return new ComposedMisspelling(word, caseSensitive, comment);
    }

    @Override
    public String getKey() {
        return this.getWord();
    }

    @Override
    public ReplacementKind getReplacementKind() {
        return ReplacementKind.COMPOSED;
    }
}
