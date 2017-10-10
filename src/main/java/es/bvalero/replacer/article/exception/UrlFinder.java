package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UrlFinder implements ErrorExceptionFinder {

    private static final String REGEX_URL = "https?://[\\w./\\-+?&%=:#;~]+";

    @Override
    public List<RegexMatch> findErrorExceptions(String text) {
        return RegExUtils.findMatches(text, REGEX_URL);
    }

}
