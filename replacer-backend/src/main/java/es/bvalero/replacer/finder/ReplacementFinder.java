package es.bvalero.replacer.finder;

import java.util.List;

/**
 * Classes implementing this interface will provide methods to find potential replacements of different types.
 */
@FunctionalInterface
public interface ReplacementFinder {

    /**
     * @return A list of potential replacements in the text.
     */
    List<Replacement> findReplacements(String text);

}
