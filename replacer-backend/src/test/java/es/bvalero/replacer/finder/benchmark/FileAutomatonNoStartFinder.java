package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.MatchResult;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FileAutomatonNoStartFinder extends FileAbstractFinder {

    private final static RunAutomaton AUTOMATON =
            new RunAutomaton(new RegExp("<L>(<L>|<N>|[. _-])+\\.<L>{2,4} *[]}|\n]").toAutomaton(new DatatypesAutomatonProvider()));

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        AutomatonMatcher m = AUTOMATON.newMatcher(text);
        while (m.find()) {
            // Remove the first and last characters and the possible surrounding spaces
            String file = m.group().substring(0, m.group().length() - 1).trim();
            int pos = m.group().indexOf(file);
            matches.add(new MatchResult(m.start() + pos, file));
        }
        return matches;
    }

}
