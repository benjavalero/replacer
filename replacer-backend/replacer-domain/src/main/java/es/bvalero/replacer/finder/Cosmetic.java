package es.bvalero.replacer.finder;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import java.util.Objects;

/**
 * A <strong>cosmetic</strong> is a special type of replacement which can be applied automatically,
 * concerning cosmetic modifications, visible or not,
 * e.g. replacing <code>[[Asia|Asia]]</code> by <code>[[Asia]]</code>.
 */
public record Cosmetic(int start, String text, String fix, CheckWikipediaAction checkWikipediaAction)
    implements FinderResult {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cosmetic cosmetic)) return false;
        return start == cosmetic.start && Objects.equals(text, cosmetic.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, text);
    }
}
