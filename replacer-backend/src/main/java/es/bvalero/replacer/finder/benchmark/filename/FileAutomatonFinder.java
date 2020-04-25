package es.bvalero.replacer.finder.benchmark.filename;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;

class FileAutomatonFinder implements BenchmarkFinder {
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX = "\\[\\[(Archivo|File|Imagen?):[^]|]+";

    private static final RunAutomaton AUTOMATON = new RunAutomaton(new RegExp(REGEX).toAutomaton());

    public Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        AutomatonMatcher m = AUTOMATON.newMatcher(text);
        while (m.find()) {
            String file = m.group();
            int colon = file.indexOf(':');
            matches.add(FinderResult.of(m.start() + colon + 1, file.substring(colon + 1)));
        }
        return matches;
    }
}
