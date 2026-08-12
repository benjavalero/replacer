package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loop over all the redirection words and find them in the lowercase text using a simple "contains".
 * Returns the whole page on first match.
 * Case-insensitive: normalizes keywords to lowercase in the constructor and converts the text to lowercase at search time.
 */
class RedirectionLowercaseContainsFinder implements BenchmarkFinder {

    private final List<String> redirectionWords = new ArrayList<>();

    RedirectionLowercaseContainsFinder(List<String> redirectionWords) {
        redirectionWords.stream().map(ReplacerUtils::toLowerCase).forEach(this.redirectionWords::add);
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final String lowerCaseText = ReplacerUtils.toLowerCase(text);
        for (String redirectionWord : redirectionWords) {
            int start = 0;
            while (start >= 0 && start < lowerCaseText.length()) {
                final int pos = lowerCaseText.indexOf(redirectionWord, start);
                if (pos < 0) break;
                if (FinderUtils.isWordCompleteInText(pos, redirectionWord, lowerCaseText)) {
                    return Stream.of(BenchmarkResult.of(0, text));
                }
                start = pos + redirectionWord.length();
            }
        }
        return Stream.of();
    }
}
