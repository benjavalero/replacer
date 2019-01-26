package es.bvalero.replacer.article;

import java.util.List;

/**
 * Classes implementing this interface will provide methods to find potential replacements of different types.
 */
public interface IArticleReplacementFinder {

    /**
     * @return A list of potential replacements in the text.
     */
    List<ArticleReplacement> findReplacements(String text);

}
