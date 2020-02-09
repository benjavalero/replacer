package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import java.util.HashSet;
import java.util.Set;

class CategoryAutomatonFinder extends CategoryAbstractFinder {
    private static final String REGEX_CATEGORY = "\\[\\[(Categoría|als):[^]]+]]";
    private static final RunAutomaton PATTERN_AUTOMATON = new RunAutomaton(new RegExp(REGEX_CATEGORY).toAutomaton());

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        AutomatonMatcher m = PATTERN_AUTOMATON.newMatcher(text);
        while (m.find()) {
            matches.add(FinderResult.of(m.start(), m.group()));
        }
        return matches;
    }
}
