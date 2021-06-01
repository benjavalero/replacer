package es.bvalero.replacer.finder.benchmark.filename;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;

class FileRegexGroupFinder implements BenchmarkFinder {

    @RegExp
    private static final String REGEX = "\\[\\[(?:Archivo|File|Imagen?):([^]|]+)";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN);
    }

    @Override
    public BenchmarkResult convert(MatchResult match) {
        return BenchmarkResult.of(match.start(1), match.group(1));
    }
}
