package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.stream.Collectors;

class RedirectionLowercaseContainsFinder implements BenchmarkFinder {

    private final Set<String> ignorableTemplates = new HashSet<>();

    RedirectionLowercaseContainsFinder(Set<String> ignorableTemplates) {
        this.ignorableTemplates.addAll(
                ignorableTemplates
                    .stream()
                    .filter(s -> s.contains("#"))
                    .map(FinderUtils::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet())
            );
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        final String lowerCaseText = FinderUtils.toLowerCase(text);
        for (String ignorableTemplate : ignorableTemplates) {
            if (lowerCaseText.contains(ignorableTemplate)) {
                return Set.of(BenchmarkResult.of(0, text));
            }
        }
        return Collections.emptySet();
    }
}
