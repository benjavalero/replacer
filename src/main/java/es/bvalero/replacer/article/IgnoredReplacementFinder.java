package es.bvalero.replacer.article;

import java.util.List;

/**
 * Classes implementing this interface will provide methods to find replacements that must be ignored.
 */
@FunctionalInterface
public interface IgnoredReplacementFinder {

    /**
     * @return A list of replacements in the text to be ignored.
     */
    List<ArticleReplacement> findIgnoredReplacements(String text);

}
