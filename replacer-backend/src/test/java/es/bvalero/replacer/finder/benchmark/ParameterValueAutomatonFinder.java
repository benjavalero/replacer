package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.MatchResult;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

class ParameterValueAutomatonFinder extends ParameterValueAbstractFinder {

    private final static RunAutomaton AUTOMATON = new RunAutomaton(
            new RegExp(String.format("\\|<Z>*(%s)<Z>*=[^|}]+", StringUtils.join(PARAMS, "|"))).toAutomaton(new DatatypesAutomatonProvider()));

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        AutomatonMatcher m = AUTOMATON.newMatcher(text);
        while (m.find()) {
            int pos = m.group().indexOf("=") + 1;
            matches.add(new MatchResult(m.start() + pos, m.group().substring(pos)));
        }
        return matches;
    }

}
