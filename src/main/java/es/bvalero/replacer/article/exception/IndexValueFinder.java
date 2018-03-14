package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class IndexValueFinder implements ExceptionMatchFinder {

    // Look-ahead as takes more time
    private static final Pattern REGEX_INDEX_VALUE = Pattern.compile("\\|\\s*índice\\s*=[^}|]*");

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, REGEX_INDEX_VALUE);
    }

}
