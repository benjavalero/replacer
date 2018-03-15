package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class TemplateNameFinder implements ExceptionMatchFinder {

    // The regex with look-behind takes the double of time: (?<=\\{\\{)[^|}]++
    // We assume there will always be two curly braces to close the template
    private static final Pattern REGEX_TEMPLATE_NAME = Pattern.compile("\\{\\{[^|}]++");

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, REGEX_TEMPLATE_NAME);
    }

}
