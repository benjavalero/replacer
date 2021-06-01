package es.bvalero.replacer.finder.benchmark.category;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;

class CategoryRegexFinder implements BenchmarkFinder {

    @RegExp
    private static final String REGEX_CATEGORY = "\\[\\[Categoría:[^]]+]]";

    private static final Pattern PATTERN_CATEGORY = Pattern.compile(REGEX_CATEGORY, Pattern.CANON_EQ);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_CATEGORY);
    }
}
