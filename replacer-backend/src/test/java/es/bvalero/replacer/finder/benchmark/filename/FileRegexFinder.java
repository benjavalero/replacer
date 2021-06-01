package es.bvalero.replacer.finder.benchmark.filename;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;

class FileRegexFinder implements BenchmarkFinder {

    @RegExp
    private static final String REGEX = "\\[\\[(?:Archivo|File|Imagen?):[^]|]+";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN);
    }

    @Override
    public BenchmarkResult convert(MatchResult match) {
        String file = match.group();
        int colon = file.indexOf(':');
        return BenchmarkResult.of(match.start() + colon + 1, file.substring(colon + 1));
    }
}
