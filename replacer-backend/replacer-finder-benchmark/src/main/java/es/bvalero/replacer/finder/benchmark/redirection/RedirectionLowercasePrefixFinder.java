package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.List;
import java.util.stream.Stream;

/**
 * Finds the common prefix of all redirection words in the lowercase text using a simple "indexOf".
 * On each prefix match, checks if any full redirection word starts at that position.
 * Returns the whole page on first match.
 * Case-insensitive: normalizes keywords to lowercase in the constructor and converts the text to lowercase at search time.
 * Requires all redirection words to share a common prefix, validated at construction time.
 */
class RedirectionLowercasePrefixFinder implements BenchmarkFinder {

    private static final String PREFIX = "#redirec";

    private final List<String> redirectionWords;

    RedirectionLowercasePrefixFinder(List<String> redirectionWords) {
        List<String> lowerCaseWords = redirectionWords.stream().map(ReplacerUtils::toLowerCase).toList();
        for (String word : lowerCaseWords) {
            if (!word.startsWith(PREFIX)) {
                throw new IllegalArgumentException(
                    "Redirection word '" + word + "' does not start with prefix '" + PREFIX + "'"
                );
            }
        }
        this.redirectionWords = lowerCaseWords;
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final String lowerCaseText = ReplacerUtils.toLowerCase(text);
        int start = 0;
        while (start >= 0 && start < lowerCaseText.length()) {
            final int pos = lowerCaseText.indexOf(PREFIX, start);
            if (pos < 0) break;
            for (String word : redirectionWords) {
                if (
                    ReplacerUtils.containsAtPosition(lowerCaseText, word, pos) &&
                    FinderUtils.isWordCompleteInText(pos, word, lowerCaseText)
                ) {
                    return Stream.of(BenchmarkResult.of(0, text));
                }
            }
            start = pos + PREFIX.length();
        }
        return Stream.of();
    }
}
