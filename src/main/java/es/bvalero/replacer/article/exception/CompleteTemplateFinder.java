package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CompleteTemplateFinder implements ExceptionMatchFinder {

    // The nested regex takes twice more but it is worth as it captures completely the templates with inner templates
    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    // The template NF usually involves ORDENAR so it is normal that the names and surnames have no diacritics
    private static final String REGEX_TEMPLATE_NAMES =
            "(ORDENAR:|DEFAULTSORT:|NF\\||[Cc]ita\\||c?[Qq]uote\\||[Cc]oord\\||[Cc]ommonscat\\|)";
    private static final RunAutomaton AUTOMATON_COMPLETE_TEMPLATE =
            new RunAutomaton(new RegExp("\\{\\{" + REGEX_TEMPLATE_NAMES + "(" + REGEX_TEMPLATE + "|[^}])+?}}").toAutomaton());

    private static final RunAutomaton AUTOMATON_CATEGORY =
            new RunAutomaton(new RegExp("\\[\\[Categoría:[^]]+?]]").toAutomaton());

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatchesAutomaton(text, AUTOMATON_COMPLETE_TEMPLATE));
        matches.addAll(RegExUtils.findMatchesAutomaton(text, AUTOMATON_CATEGORY));
        return matches;
    }

}
