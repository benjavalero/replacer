package es.bvalero.replacer.finder.benchmark.category;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import java.util.regex.MatchResult;

class CategoryAutomatonFinder implements BenchmarkFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_CATEGORY = "\\[\\[Categoría:[^]]+]]";

    private static final RunAutomaton AUTOMATON_CATEGORY = new RunAutomaton(new RegExp(REGEX_CATEGORY).toAutomaton());

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), AUTOMATON_CATEGORY);
    }
}
