package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.finder.BenchmarkResult;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.HashSet;
import java.util.Set;

class RedirectionLowercaseContainsFinder implements BenchmarkFinder {

    private final Set<String> ignorableTemplates = new HashSet<>();

    RedirectionLowercaseContainsFinder(Set<String> ignorableTemplates) {
        this.ignorableTemplates.addAll(ignorableTemplates);
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final String lowerCaseText = FinderUtils.toLowerCase(text);
        for (String ignorableTemplate : ignorableTemplates) {
            if (lowerCaseText.contains(ignorableTemplate)) {
                return Set.of(BenchmarkResult.of(0, text));
            }
        }
        return Set.of();
    }
}
