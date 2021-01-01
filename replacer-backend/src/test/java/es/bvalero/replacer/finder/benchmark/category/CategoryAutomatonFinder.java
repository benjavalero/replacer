package es.bvalero.replacer.finder.benchmark.category;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.apache.commons.collections4.IterableUtils;

class CategoryAutomatonFinder implements BenchmarkFinder {
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_CATEGORY = "\\[\\[Categoría:[^]]+]]";

    private static final RunAutomaton AUTOMATON_CATEGORY = new RunAutomaton(new RegExp(REGEX_CATEGORY).toAutomaton());

    public Set<FinderResult> findMatches(String text) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(WikipediaLanguage.getDefault()).build();
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(page, AUTOMATON_CATEGORY, this::convert)));
    }
}
