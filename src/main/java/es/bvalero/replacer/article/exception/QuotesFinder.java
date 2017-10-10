package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class QuotesFinder implements ErrorExceptionFinder {

    // We trust the quotes are well formed with matching leading and trailing quotes
    private static final String REGEX_QUOTES = "'{2,5}.+?'{2,5}";
    private static final String REGEX_QUOTES_ESCAPED = "(&apos;){2,5}.+?(&apos;){2,5}";
    private static final String REGEX_ANGULAR_QUOTES = "«[^»]+»";
    private static final String REGEX_TYPOGRAPHIC_QUOTES = "“[^”]+”";
    private static final String REGEX_DOUBLE_QUOTES = "\"[^\"]+\"";
    private static final String REGEX_DOUBLE_QUOTES_ESCAPED = "&quot;.+?&quot;";

    @Override
    public List<RegexMatch> findErrorExceptions(String text) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, REGEX_QUOTES));
        matches.addAll(RegExUtils.findMatches(text, REGEX_QUOTES_ESCAPED));
        matches.addAll(RegExUtils.findMatches(text, REGEX_ANGULAR_QUOTES));
        matches.addAll(RegExUtils.findMatches(text, REGEX_TYPOGRAPHIC_QUOTES));
        matches.addAll(RegExUtils.findMatches(text, REGEX_DOUBLE_QUOTES));
        matches.addAll(RegExUtils.findMatches(text, REGEX_DOUBLE_QUOTES_ESCAPED));
        return matches;
    }

}
