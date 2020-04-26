package es.bvalero.replacer.finder.benchmark.cursive;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;

class CursiveAutomatonFinder implements BenchmarkFinder {
    private static final String TWO_QUOTES_ONLY = "[^']''[^']";
    private static final String CURSIVE_REGEX = "%s(('''''|'''|')?[^'\n])*(%s|\n)";
    private static final RunAutomaton CURSIVE_AUTOMATON = new RunAutomaton(
        new RegExp(String.format(CURSIVE_REGEX, TWO_QUOTES_ONLY, TWO_QUOTES_ONLY)).toAutomaton()
    );

    public Set<FinderResult> findMatches(String text) {
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(text, CURSIVE_AUTOMATON, this::convert)));
    }

    @Override
    public FinderResult convert(MatchResult match) {
        int start = match.start() + 1;
        int end = match.group().endsWith("\n") ? match.group().length() : match.group().length() - 1;
        String group = match.group().substring(1, end);
        return FinderResult.of(start, group);
    }
}
