package es.bvalero.replacer.finder.benchmark.category;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.page.IndexablePage;
import java.util.regex.MatchResult;

class CategoryAutomatonFinder implements BenchmarkFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_CATEGORY = "\\[\\[Categoría:[^]]+]]";

    private static final RunAutomaton AUTOMATON_CATEGORY = new RunAutomaton(new RegExp(REGEX_CATEGORY).toAutomaton());

    @Override
    public Iterable<MatchResult> findMatchResults(IndexablePage page) {
        return AutomatonMatchFinder.find(page.getContent(), AUTOMATON_CATEGORY);
    }
}
