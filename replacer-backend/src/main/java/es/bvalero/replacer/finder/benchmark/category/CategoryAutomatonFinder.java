package es.bvalero.replacer.finder.benchmark.category;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;

class CategoryAutomatonFinder implements BenchmarkFinder {
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_CATEGORY = "\\[\\[(Categoría|als):[^]]+]]";

    private static final RunAutomaton PATTERN_AUTOMATON = new RunAutomaton(new RegExp(REGEX_CATEGORY).toAutomaton());

    public Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        AutomatonMatcher m = PATTERN_AUTOMATON.newMatcher(text);
        while (m.find()) {
            matches.add(FinderResult.of(m.start(), m.group()));
        }
        return matches;
    }
}
