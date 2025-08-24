package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class RedirectionRegexFinder implements BenchmarkFinder {

    private final Pattern pattern;

    RedirectionRegexFinder(List<String> redirectionWords) {
        String alternations = '(' + FinderUtils.joinAlternate(redirectionWords) + ")";
        this.pattern = Pattern.compile(alternations);
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final String lowerCaseText = FinderUtils.toLowerCase(text);
        final Matcher m = this.pattern.matcher(lowerCaseText);
        if (m.find()) {
            return Stream.of(BenchmarkResult.of(0, text));
        }
        return Stream.of();
    }
}
