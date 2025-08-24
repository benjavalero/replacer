package es.bvalero.replacer.finder.benchmark.simple;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

class SimpleAutomatonFinder implements BenchmarkFinder {

    private final RunAutomaton automaton;

    SimpleAutomatonFinder(String word) {
        this.automaton = new RunAutomaton(new RegExp(word).toAutomaton());
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), automaton);
    }
}
