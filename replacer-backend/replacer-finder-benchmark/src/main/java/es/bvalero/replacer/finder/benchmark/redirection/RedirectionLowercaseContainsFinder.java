package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class RedirectionLowercaseContainsFinder implements BenchmarkFinder {

    private final List<String> redirectionWords = new ArrayList<>();

    RedirectionLowercaseContainsFinder(List<String> redirectionWords) {
        this.redirectionWords.addAll(redirectionWords);
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final String lowerCaseText = ReplacerUtils.toLowerCase(text);
        for (String redirectionWord : redirectionWords) {
            if (lowerCaseText.contains(redirectionWord)) {
                return Stream.of(BenchmarkResult.of(0, text));
            }
        }
        return Stream.of();
    }
}
