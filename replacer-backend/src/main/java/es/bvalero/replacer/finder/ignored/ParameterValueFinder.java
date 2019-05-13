package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import es.bvalero.replacer.finder.ReplacementFinder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ParameterValueFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    // Look-ahead takes more time
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_INDEX_VALUE = "\\|<Z>*(índice|index|cita|location|ubicación)<Z>*=[^}|]+";
    private static final RunAutomaton AUTOMATON_INDEX_VALUE =
            new RunAutomaton(new RegExp(REGEX_INDEX_VALUE).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);

        for (MatchResult match : findMatchResults(text, AUTOMATON_INDEX_VALUE)) {
            int posEquals = match.getText().indexOf('=') + 1;
            String fileName = match.getText().substring(posEquals).trim();
            matches.add(new MatchResult(match.getStart() + match.getText().indexOf(fileName), fileName));
        }

        return matches;
    }

}
