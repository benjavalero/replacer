package es.bvalero.replacer.finder2;

import es.bvalero.replacer.finder.Replacement;
import java.util.stream.Stream;

/**
 * Interface to be implemented by any class returning a collection of replacements.
 */
public interface ReplacementFinder {
    public Stream<Replacement> findReplacements(String text);
}
