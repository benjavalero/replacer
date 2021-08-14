package es.bvalero.replacer.finder.listing.parse;

import es.bvalero.replacer.finder.listing.ComposedMisspelling;

public class ComposedMisspellingParser implements MisspellingParser<ComposedMisspelling> {

    @Override
    public ComposedMisspelling buildMisspelling(String word, boolean cs, String comment) {
        return ComposedMisspelling.of(word, cs, comment);
    }
}
