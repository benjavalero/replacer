package es.bvalero.replacer.finder.listing.parse;

import es.bvalero.replacer.finder.listing.SimpleMisspelling;

public class SimpleMisspellingParser implements MisspellingParser<SimpleMisspelling> {

    @Override
    public SimpleMisspelling buildMisspelling(String word, boolean cs, String comment) {
        return SimpleMisspelling.of(word, cs, comment);
    }
}
