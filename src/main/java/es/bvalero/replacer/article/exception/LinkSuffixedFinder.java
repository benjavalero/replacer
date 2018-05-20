package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class LinkSuffixedFinder implements ExceptionMatchFinder {

    private static final Pattern REGEX_LINK_SUFFIXED =
            Pattern.compile("\\[\\[\\p{L}+]]\\p{Ll}+");

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, REGEX_LINK_SUFFIXED);
    }

}
