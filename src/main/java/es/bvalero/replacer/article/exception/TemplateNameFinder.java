package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class TemplateNameFinder implements ExceptionMatchFinder {

    private static final RunAutomaton AUTOMATON_TEMPLATE_NAME =
            new RunAutomaton(new RegExp("\\{\\{[^|}:]+").toAutomaton());

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatchesAutomaton(text, AUTOMATON_TEMPLATE_NAME);
    }

}
