package es.bvalero.replacer.finder.replacement.custom;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.finder.listing.Misspelling;

class CustomMisspelling extends Misspelling {

    private CustomMisspelling(String word, boolean caseSensitive, String comment) {
        super(word, caseSensitive, comment);
    }

    public static CustomMisspelling of(CustomType customType) {
        return new CustomMisspelling(customType.getSubtype(), customType.isCaseSensitive(), customType.getSuggestion());
    }
}
