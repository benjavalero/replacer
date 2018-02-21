package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IndexValueMatchFinder implements ExceptionMatchFinder {

    private static final String REGEX_INDEX_VALUE = "\\|\\s*(?:índice)\\s*=[^}|]*";

    @Override
    public List<RegexMatch> findExceptionMatches(String text) {
        return RegExUtils.findMatches(text, REGEX_INDEX_VALUE);
    }

}
