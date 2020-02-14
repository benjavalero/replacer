package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

class ParameterValueAutomatonFinder extends ParameterValueAbstractFinder {
    private static final RunAutomaton AUTOMATON = new RunAutomaton(
        new RegExp(String.format("\\|<Z>*(%s)<Z>*=[^|}]+", StringUtils.join(PARAMS, "|")))
        .toAutomaton(new DatatypesAutomatonProvider())
    );

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        AutomatonMatcher m = AUTOMATON.newMatcher(text);
        while (m.find()) {
            int pos = m.group().indexOf("=") + 1;
            matches.add(FinderResult.of(m.start() + pos, m.group().substring(pos)));
        }
        return matches;
    }
}
