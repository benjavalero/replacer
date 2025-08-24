package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class RedirectionRegexInsensitiveFinder implements BenchmarkFinder {

    private final Pattern pattern;

    RedirectionRegexInsensitiveFinder(List<String> redirectionWords) {
        String alternations = '(' + FinderUtils.joinAlternate(redirectionWords) + ")";
        this.pattern = Pattern.compile(alternations, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final Matcher m = this.pattern.matcher(text);
        if (m.find()) {
            return Stream.of(BenchmarkResult.of(0, text));
        }
        return Stream.of();
    }
}
