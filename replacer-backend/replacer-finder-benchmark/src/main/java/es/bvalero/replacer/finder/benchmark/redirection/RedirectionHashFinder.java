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
 * Finds the '#' character and checks if the following characters match any redirection word.
 * Uses a precomputed max-length to extract only the necessary substring before lowercasing.
 * Returns the whole page on first match.
 * Case-insensitive: normalizes keywords to lowercase in the constructor and converts the candidate substring to lowercase at search time.
 */
class RedirectionHashFinder implements BenchmarkFinder {

    private final List<String> redirectionWords = new ArrayList<>();
    private final int maxWordLength;

    RedirectionHashFinder(List<String> redirectionWords) {
        redirectionWords.stream().map(ReplacerUtils::toLowerCase).forEach(this.redirectionWords::add);
        this.maxWordLength = this.redirectionWords.stream().mapToInt(String::length).max().orElse(0);
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        int start = 0;
        while (start < text.length()) {
            final int pos = text.indexOf('#', start);
            if (pos < 0) break;
            final int end = Math.min(pos + maxWordLength, text.length());
            final String candidate = ReplacerUtils.toLowerCase(text.substring(pos, end));
            for (String word : redirectionWords) {
                if (candidate.startsWith(word) && FinderUtils.isWordCompleteInText(pos, word, text)) {
                    return Stream.of(BenchmarkResult.of(0, text));
                }
            }
            start = pos + 1;
        }
        return Stream.of();
    }
}
