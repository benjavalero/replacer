package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.finder.BenchmarkResult;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class RedirectionLowercaseContainsFinder implements BenchmarkFinder {

    private final List<String> redirectionWords = new ArrayList<>();

    RedirectionLowercaseContainsFinder(List<String> redirectionWords) {
        this.redirectionWords.addAll(redirectionWords);
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final String lowerCaseText = FinderUtils.toLowerCase(text);
        for (String redirectionWord : redirectionWords) {
            if (lowerCaseText.contains(redirectionWord)) {
                return Set.of(BenchmarkResult.of(0, text));
            }
        }
        return Set.of();
    }
}
