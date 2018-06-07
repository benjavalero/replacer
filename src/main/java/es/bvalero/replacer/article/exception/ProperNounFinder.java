package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class ProperNounFinder implements ExceptionMatchFinder {

    private static final Pattern REGEX_PROPER_NOUN =
            Pattern.compile("\\b(Domingo|Julio) (?=\\p{Lu})");

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, REGEX_PROPER_NOUN);
    }

}
