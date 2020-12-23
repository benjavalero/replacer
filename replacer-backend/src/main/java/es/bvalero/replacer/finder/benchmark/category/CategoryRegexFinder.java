package es.bvalero.replacer.finder.benchmark.category;

import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.apache.commons.collections4.IterableUtils;
import org.intellij.lang.annotations.RegExp;

class CategoryRegexFinder implements BenchmarkFinder {
    @RegExp
    private static final String REGEX_CATEGORY = "\\[\\[Categoría:[^]]+]]";

    private static final Pattern PATTERN_CATEGORY = Pattern.compile(REGEX_CATEGORY, Pattern.CANON_EQ);

    public Set<FinderResult> findMatches(String text) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(WikipediaLanguage.getDefault()).build();
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(page, PATTERN_CATEGORY, this::convert)));
    }
}
