package es.bvalero.replacer.finder.benchmark.simple;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import java.util.regex.MatchResult;

class SimpleAutomatonFinder implements BenchmarkFinder {

    private final RunAutomaton automaton;

    SimpleAutomatonFinder(String word) {
        this.automaton = new RunAutomaton(new RegExp(word).toAutomaton());
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return AutomatonMatchFinder.find(page.getContent(), automaton);
    }
}
