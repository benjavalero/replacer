package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UrlFinder implements ExceptionMatchFinder {

    // Copied from pywikibot
    private static final String AT_END = "[^]\\s.:;,<>\"|)]";
    private static final String INSIDE = "[^]\\s<>\"|]";
    private static final String INSIDE_ESCAPED = "(" + INSIDE + "(?!&lt;))";
    private static final String REGEX_URL = "http[s]?://" + INSIDE + "*" + AT_END;
    private static final String REGEX_URL_ESCAPED = "http[s]?://" + INSIDE_ESCAPED + "*" + AT_END;

    private static final String REGEX_DOMAIN = "\\b(?:\\w+\\.)+(?:com?|org|net|info|es)\\b";

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, isTextEscaped ? REGEX_URL_ESCAPED : REGEX_URL));
        matches.addAll(RegExUtils.findMatches(text, REGEX_DOMAIN));
        return matches;
    }

}
