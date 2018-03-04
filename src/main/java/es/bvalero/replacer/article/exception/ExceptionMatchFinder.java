package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;

import java.util.List;

public interface ExceptionMatchFinder {

    List<RegexMatch> findExceptionMatches(String text);

}
