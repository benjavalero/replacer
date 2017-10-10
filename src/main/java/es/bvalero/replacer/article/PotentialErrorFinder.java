package es.bvalero.replacer.article;

import java.util.List;

public interface PotentialErrorFinder {

    List<ArticleReplacement> findPotentialErrors(String text);

}
