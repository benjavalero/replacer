package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.MatchResult;

import java.util.HashSet;
import java.util.Set;

class CursiveAutomatonFinder extends CursiveAbstractFinder {

    private static final String BOLD_TEMPLATE = "'{3,}[^']+{3,}";
    private final static RunAutomaton CURSIVE_AUTOMATON = new RunAutomaton(new RegExp(String.format("''(%s|[^'\n])+(''|\n)", BOLD_TEMPLATE)).toAutomaton());

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        AutomatonMatcher m = CURSIVE_AUTOMATON.newMatcher(text);
        while (m.find()) {
            matches.add(new MatchResult(m.start(), m.group()));
        }
        return matches;
    }

}
