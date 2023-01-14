package es.bvalero.replacer.finder.benchmark.cursive;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.BenchmarkResult;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import java.util.regex.MatchResult;

class CursiveAutomatonFinder implements BenchmarkFinder {

    private static final String TWO_QUOTES_ONLY = "[^']''[^']";
    private static final String CURSIVE_REGEX = "%s(('''''|'''|')?[^'\n])*(%s|\n)";
    private static final RunAutomaton CURSIVE_AUTOMATON = new RunAutomaton(
        new RegExp(String.format(CURSIVE_REGEX, TWO_QUOTES_ONLY, TWO_QUOTES_ONLY)).toAutomaton()
    );

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), CURSIVE_AUTOMATON);
    }

    @Override
    public BenchmarkResult convert(MatchResult match) {
        int start = match.start() + 1;
        int end = match.group().endsWith("\n") ? match.group().length() : match.group().length() - 1;
        String group = match.group().substring(1, end);
        return BenchmarkResult.of(start, group);
    }
}
