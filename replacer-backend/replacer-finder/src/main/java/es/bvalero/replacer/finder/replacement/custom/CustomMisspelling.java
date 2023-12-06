package es.bvalero.replacer.finder.replacement.custom;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.finder.Misspelling;

class CustomMisspelling extends Misspelling {

    private CustomMisspelling(String word, boolean caseSensitive, String comment) {
        super(word, caseSensitive, comment);
    }

    static CustomMisspelling of(String word, boolean caseSensitive, String comment) {
        return new CustomMisspelling(word, caseSensitive, comment);
    }

    CustomType toCustomType() {
        return CustomType.of(this.getWord(), this.isCaseSensitive());
    }
}
