package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.finder.BenchmarkResult;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class RedirectionRegexFinder implements BenchmarkFinder {

    private final Pattern pattern;

    RedirectionRegexFinder(Set<String> ignorableTemplates) {
        Set<String> fixedTemplates = ignorableTemplates
            .stream()
            .filter(s -> s.contains("#"))
            .map(FinderUtils::toLowerCase)
            .collect(Collectors.toSet());
        String alternations = '(' + FinderUtils.joinAlternate(fixedTemplates) + ")";
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
        return Collections.emptyList();
    }
}
