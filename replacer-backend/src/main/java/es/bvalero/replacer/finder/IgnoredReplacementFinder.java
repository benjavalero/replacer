package es.bvalero.replacer.finder;

import java.util.List;

/**
 * Classes implementing this interface will provide a method to find places in the text that must be ignored.
 */
@FunctionalInterface
public interface IgnoredReplacementFinder {

    /**
     * @return A list of places in the text to be ignored.
     */
    List<MatchResult> findIgnoredReplacements(String text);

}
