package es.bvalero.replacer.finder.benchmark.category;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.IterableUtils;

class CategoryAutomatonFinder implements BenchmarkFinder {
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_CATEGORY = "\\[\\[(Categoría|als):[^]]+]]";

    private static final RunAutomaton AUTOMATON_CATEGORY = new RunAutomaton(new RegExp(REGEX_CATEGORY).toAutomaton());

    public Set<FinderResult> findMatches(String text) {
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(text, AUTOMATON_CATEGORY, this::convert)));
    }
}
