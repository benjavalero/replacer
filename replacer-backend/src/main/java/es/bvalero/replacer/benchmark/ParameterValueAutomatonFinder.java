package es.bvalero.replacer.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacement;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

class ParameterValueAutomatonFinder extends ParameterValueAbstractFinder {

    private static final RunAutomaton AUTOMATON = new RunAutomaton(
            new RegExp(String.format("\\|<Z>*(%s)<Z>*=[^|}]+", StringUtils.join(PARAMS, "|"))).toAutomaton(new DatatypesAutomatonProvider()));

    Set<IgnoredReplacement> findMatches(String text) {
        Set<IgnoredReplacement> matches = new HashSet<>();
        AutomatonMatcher m = AUTOMATON.newMatcher(text);
        while (m.find()) {
            int pos = m.group().indexOf("=") + 1;
            matches.add(IgnoredReplacement.of(m.start() + pos, m.group().substring(pos)));
        }
        return matches;
    }

}
