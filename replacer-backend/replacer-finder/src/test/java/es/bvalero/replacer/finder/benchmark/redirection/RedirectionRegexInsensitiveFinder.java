package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.finder.BenchmarkResult;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RedirectionRegexInsensitiveFinder implements BenchmarkFinder {

    private final Pattern pattern;

    RedirectionRegexInsensitiveFinder(Set<String> ignorableTemplates) {
        String alternations = '(' + FinderUtils.joinAlternate(ignorableTemplates) + ")";
        this.pattern = Pattern.compile(alternations, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final Matcher m = this.pattern.matcher(text);
        if (m.find()) {
            return List.of(BenchmarkResult.of(0, text));
        }
        return List.of();
    }
}
