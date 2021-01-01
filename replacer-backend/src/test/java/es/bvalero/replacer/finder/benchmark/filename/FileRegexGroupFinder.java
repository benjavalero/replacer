package es.bvalero.replacer.finder.benchmark.filename;

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
import org.intellij.lang.annotations.RegExp;

class FileRegexGroupFinder implements BenchmarkFinder {
    @RegExp
    private static final String REGEX = "\\[\\[(?:Archivo|File|Imagen?):([^]|]+)";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public Set<FinderResult> findMatches(String text) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(WikipediaLanguage.getDefault()).build();
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(page, PATTERN, this::convert)));
    }

    @Override
    public FinderResult convert(MatchResult match) {
        return FinderResult.of(match.start(1), match.group(1));
    }
}
