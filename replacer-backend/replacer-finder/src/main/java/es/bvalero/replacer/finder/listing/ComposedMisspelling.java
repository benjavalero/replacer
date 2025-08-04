package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.finder.ReplacementKind;

public class ComposedMisspelling extends StandardMisspelling {

    private ComposedMisspelling(String word, boolean caseSensitive, String comment) {
        super(word, caseSensitive, comment);
    }

    public static ComposedMisspelling of(String word, boolean caseSensitive, String comment) {
        return new ComposedMisspelling(word, caseSensitive, comment);
    }

    @Override
    public ReplacementKind getReplacementKind() {
        return ReplacementKind.COMPOSED;
    }
}
