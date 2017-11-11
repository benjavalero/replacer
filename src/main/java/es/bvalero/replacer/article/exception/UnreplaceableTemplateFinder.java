package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UnreplaceableTemplateFinder implements ErrorExceptionFinder {

    private static final String REGEX_UNREPLACEBLE_TEMPLATE =
            "\\{\\{(?:ORDENAR:|DEFAULTSORT:|NF\\||[Cc]ita\\||c?[Qq]uote\\||[Cc]oord\\||[Cc]ommonscat\\|)[^}]+}}";
    private static final String REGEX_CATEGORY = "\\[\\[Categoría:[^]]+]]";

    @Override
    public List<RegexMatch> findErrorExceptions(String text) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, REGEX_UNREPLACEBLE_TEMPLATE));
        // TODO Expand the match in case the template contains another template
        matches.addAll(RegExUtils.findMatches(text, REGEX_CATEGORY));
        return matches;
    }

}
