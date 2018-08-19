package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TemplateParamFinder implements ExceptionMatchFinder {

    private static final RunAutomaton AUTOMATON_TEMPLATE_PARAM =
            new RunAutomaton(new RegExp("\\|\\s*(<L>|<N>|[ _-])+?\\s*=").toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatchesAutomaton(text, AUTOMATON_TEMPLATE_PARAM);
    }

}
