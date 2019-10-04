package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.HashSet;
import java.util.Set;

class FileAutomatonNoStartFinder extends FileAbstractFinder {

    private static final RunAutomaton AUTOMATON =
            new RunAutomaton(new RegExp("<L>(<L>|<N>|[. _-])+\\.<L>{2,4} *[]}|\n]").toAutomaton(new DatatypesAutomatonProvider()));

    Set<IgnoredReplacement> findMatches(String text) {
        Set<IgnoredReplacement> matches = new HashSet<>();
        AutomatonMatcher m = AUTOMATON.newMatcher(text);
        while (m.find()) {
            // Remove the first and last characters and the possible surrounding spaces
            String file = m.group().substring(0, m.group().length() - 1).trim();
            int pos = m.group().indexOf(file);
            matches.add(IgnoredReplacement.of(m.start() + pos, file));
        }
        return matches;
    }

}
