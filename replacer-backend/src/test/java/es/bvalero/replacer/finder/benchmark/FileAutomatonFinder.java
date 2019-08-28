package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.MatchResult;

import java.util.HashSet;
import java.util.Set;

class FileAutomatonFinder extends FileAbstractFinder {

    private static final RunAutomaton AUTOMATON =
            new RunAutomaton(new RegExp("[:=|\n] *[^]:=|\n]+\\.[A-Za-z]{2,4} *[]}|\n]")
                    .toAutomaton(new DatatypesAutomatonProvider()));

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        AutomatonMatcher m = AUTOMATON.newMatcher(text);
        while (m.find()) {
            // Remove the first and last characters and the possible surrounding spaces
            String file = m.group().substring(1, m.group().length() - 1).trim();
            int pos = m.group().indexOf(file);
            matches.add(MatchResult.of(m.start() + pos, file));
        }
        return matches;
    }

}
