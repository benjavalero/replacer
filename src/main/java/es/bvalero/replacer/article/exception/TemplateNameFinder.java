package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TemplateNameFinder implements ExceptionMatchFinder {

    // The regex with look-behind takes the double of time: (?<=\\{\\{)[^|}]++
    // We assume there will always be two curly braces to close the template
    private static final String REGEX_TEMPLATE_NAME = "\\{\\{[^|}]++";

    @Override
    public List<RegexMatch> findExceptionMatches(String text) {
        return RegExUtils.findMatches(text, REGEX_TEMPLATE_NAME);
    }

}
