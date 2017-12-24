package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UrlFinder implements ErrorExceptionFinder {

    private static final String REGEX_URL = "https?://[\\w./\\-+?&%=:#;,~!]+";
    private static final String REGEX_DOMAIN = "[a-z]+\\.(?:com|org|net|info|es)";

    @Override
    public List<RegexMatch> findErrorExceptions(String text) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, REGEX_URL));
        matches.addAll(RegExUtils.findMatches(text, REGEX_DOMAIN));
        return matches;
    }

}
