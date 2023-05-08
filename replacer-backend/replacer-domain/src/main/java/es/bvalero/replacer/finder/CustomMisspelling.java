package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.CustomType;

public class CustomMisspelling extends Misspelling {

    private CustomMisspelling(String word, boolean caseSensitive, String comment) {
        super(word, caseSensitive, comment);
    }

    public static CustomMisspelling of(CustomType customType) {
        return new CustomMisspelling(customType.getSubtype(), customType.isCaseSensitive(), customType.getSuggestion());
    }
}
