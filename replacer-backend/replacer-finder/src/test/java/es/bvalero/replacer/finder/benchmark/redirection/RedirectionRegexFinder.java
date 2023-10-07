package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.finder.BenchmarkResult;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RedirectionRegexFinder implements BenchmarkFinder {

    private final Pattern pattern;

    RedirectionRegexFinder(Set<String> ignorableTemplates) {
        String alternations = '(' + FinderUtils.joinAlternate(ignorableTemplates) + ")";
        this.pattern = Pattern.compile(alternations);
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final String lowerCaseText = FinderUtils.toLowerCase(text);
        final Matcher m = this.pattern.matcher(lowerCaseText);
        if (m.find()) {
            return List.of(BenchmarkResult.of(0, text));
        }
        return List.of();
    }
}
