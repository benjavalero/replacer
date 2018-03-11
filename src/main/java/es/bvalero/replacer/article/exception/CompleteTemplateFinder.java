package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompleteTemplateFinder implements ExceptionMatchFinder {

    // The nested regex takes twice more but it is worth as it captures completely the templates with inner templates
    private static final String REGEX_TEMPLATE = "\\{\\{[^}]++}}";
    private static final String REGEX_TEMPLATE_NAMES = "(?:ORDENAR:|DEFAULTSORT:|NF\\||[Cc]ita\\||c?[Qq]uote\\||[Cc]oord\\||[Cc]ommonscat\\|)";
    private static final String REGEX_COMPLETE_TEMPLATE = "\\{\\{" + REGEX_TEMPLATE_NAMES + "(" + REGEX_TEMPLATE + "|[^}])++}}";

    private static final String REGEX_CATEGORY = "\\[\\[Categoría:[^]]++]]";

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, REGEX_COMPLETE_TEMPLATE));
        matches.addAll(RegExUtils.findMatches(text, REGEX_CATEGORY));
        return matches;
    }

}
