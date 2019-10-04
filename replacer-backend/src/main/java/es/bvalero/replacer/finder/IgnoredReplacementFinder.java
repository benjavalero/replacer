package es.bvalero.replacer.finder;

import java.util.List;

/**
 * Classes implementing this interface will provide a method to find places in the text that must be ignored.
 */
public interface IgnoredReplacementFinder extends BaseFinder<IgnoredReplacement> {

    /**
     * @return A list of places in the text to be ignored.
     */
    List<IgnoredReplacement> findIgnoredReplacements(String text);

    @Override
    default boolean isValidMatch(int start, String matchedText, String fullText) {
        return true;
    }

    @Override
    default IgnoredReplacement convertMatch(int start, String text) {
        return IgnoredReplacement.of(start, text);
    }

}
