package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;

import java.util.List;

public interface ErrorExceptionFinder {

    List<RegexMatch> findErrorExceptions(String text);

}
