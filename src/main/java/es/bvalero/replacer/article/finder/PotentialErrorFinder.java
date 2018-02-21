package es.bvalero.replacer.article.finder;

import es.bvalero.replacer.article.ArticleReplacement;

import java.util.List;

public interface PotentialErrorFinder {

    /**
     * @return A list of potential replacements in the text.
     */
    List<ArticleReplacement> findPotentialErrors(String text);

}
