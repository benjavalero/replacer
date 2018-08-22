package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IndexValueFinder implements ExceptionMatchFinder {

    // Look-ahead as takes more time
    private static final RunAutomaton AUTOMATON_INDEX_VALUE =
            new RunAutomaton(new RegExp("\\|<Z>*(índice|index|cita)<Z>*=[^}|]+").toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatchesAutomaton(text, AUTOMATON_INDEX_VALUE);
    }

}
