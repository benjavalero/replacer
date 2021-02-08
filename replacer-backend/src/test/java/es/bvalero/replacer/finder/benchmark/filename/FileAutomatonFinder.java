package es.bvalero.replacer.finder.benchmark.filename;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.page.IndexablePage;
import java.util.regex.MatchResult;

class FileAutomatonFinder implements BenchmarkFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX = "\\[\\[(Archivo|File|Imagen?):[^]|]+";

    private static final RunAutomaton AUTOMATON = new RunAutomaton(new RegExp(REGEX).toAutomaton());

    @Override
    public Iterable<MatchResult> findMatchResults(IndexablePage page) {
        return AutomatonMatchFinder.find(page.getContent(), AUTOMATON);
    }

    @Override
    public BenchmarkResult convert(MatchResult match) {
        String file = match.group();
        int colon = file.indexOf(':');
        return BenchmarkResult.of(match.start() + colon + 1, file.substring(colon + 1));
    }
}
