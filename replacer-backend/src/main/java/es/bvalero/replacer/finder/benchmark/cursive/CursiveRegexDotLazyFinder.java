package es.bvalero.replacer.finder.benchmark.cursive;

import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.apache.commons.collections4.IterableUtils;

class CursiveRegexDotLazyFinder implements BenchmarkFinder {
    private static final String TWO_QUOTES_ONLY = "[^']''[^']";
    private static final String CURSIVE_REGEX = "%s.*?(%s|\n)";
    private static final Pattern CURSIVE_PATTERN = Pattern.compile(
        String.format(CURSIVE_REGEX, TWO_QUOTES_ONLY, TWO_QUOTES_ONLY)
    );

    @Override
    public Set<FinderResult> findMatches(String text) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(WikipediaLanguage.getDefault()).build();
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(page, CURSIVE_PATTERN, this::convert)));
    }

    @Override
    public FinderResult convert(MatchResult match) {
        int start = match.start() + 1;
        int end = match.group().endsWith("\n") ? match.group().length() : match.group().length() - 1;
        String group = match.group().substring(1, end);
        return FinderResult.of(start, group);
    }
}
