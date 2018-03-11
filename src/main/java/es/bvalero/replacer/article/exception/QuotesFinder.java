package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class QuotesFinder implements ExceptionMatchFinder {

    private static final String REGEX_SINGLE_QUOTES = "('{2,5}+).+?[^']\\1(?!')";
    private static final String REGEX_SINGLE_QUOTES_ESCAPED = "((&apos;){2,5}+).+?(?<!&apos;)\\1(?!&apos;)";

    // The conditional regex to combine both below takes 4 times more: (?:(«)|“).+?(?(1)»|”)
    private static final String REGEX_ANGULAR_QUOTES = "«[^»]++»";
    private static final String REGEX_TYPOGRAPHIC_QUOTES = "“[^”]++”";

    private static final String REGEX_DOUBLE_QUOTES = "\"[^\"]++\"";
    private static final String REGEX_DOUBLE_QUOTES_ESCAPED = "&quot;.+?&quot;";

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        if (isTextEscaped) {
            // There are lots of lines with single quotes not closed
            matches.addAll(RegExUtils.findMatches(text, REGEX_SINGLE_QUOTES_ESCAPED));
            matches.addAll(RegExUtils.findMatches(text, REGEX_DOUBLE_QUOTES_ESCAPED, Pattern.DOTALL));
        } else {
            matches.addAll(RegExUtils.findMatches(text, REGEX_SINGLE_QUOTES));
            matches.addAll(RegExUtils.findMatches(text, REGEX_DOUBLE_QUOTES));
        }
        matches.addAll(RegExUtils.findMatches(text, REGEX_ANGULAR_QUOTES));
        matches.addAll(RegExUtils.findMatches(text, REGEX_TYPOGRAPHIC_QUOTES));
        return matches;
    }

}
