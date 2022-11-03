package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class RedirectionRegexInsensitiveFinder implements BenchmarkFinder {

    private final Pattern pattern;

    RedirectionRegexInsensitiveFinder(Set<String> ignorableTemplates) {
        Set<String> fixedTemplates = ignorableTemplates
            .stream()
            .filter(s -> s.contains("#"))
            .map(FinderUtils::toLowerCase)
            .collect(Collectors.toSet());
        String alternations = '(' + FinderUtils.joinAlternate(fixedTemplates) + ")";
        this.pattern = Pattern.compile(alternations, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        final Matcher m = this.pattern.matcher(text);
        if (m.find()) {
            return List.of(BenchmarkResult.of(0, text));
        }
        return Collections.emptyList();
    }
}
