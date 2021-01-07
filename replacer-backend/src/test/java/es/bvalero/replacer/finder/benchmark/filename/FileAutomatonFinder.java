package es.bvalero.replacer.finder.benchmark.filename;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;

class FileAutomatonFinder implements BenchmarkFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX = "\\[\\[(Archivo|File|Imagen?):[^]|]+";

    private static final RunAutomaton AUTOMATON = new RunAutomaton(new RegExp(REGEX).toAutomaton());

    public Set<FinderResult> findMatches(String text) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(WikipediaLanguage.getDefault()).build();
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(page, AUTOMATON, this::convert)));
    }

    @Override
    public FinderResult convert(MatchResult match) {
        String file = match.group();
        int colon = file.indexOf(':');
        return FinderResult.of(match.start() + colon + 1, file.substring(colon + 1));
    }
}
