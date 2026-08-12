package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Finds the redirection words in the lowercase text using a regex alternation.
 * Returns the whole page on first match.
 * Case-insensitive: normalizes keywords to lowercase in the constructor and converts the text to lowercase at search time.
 */
class RedirectionRegexFinder implements BenchmarkFinder {

    private final Pattern pattern;

    RedirectionRegexFinder(List<String> redirectionWords) {
        List<String> lowerCaseWords = redirectionWords.stream().map(ReplacerUtils::toLowerCase).toList();
        this.pattern = Pattern.compile('(' + FinderUtils.joinAlternate(lowerCaseWords) + ')');
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final String lowerCaseText = ReplacerUtils.toLowerCase(text);
        final Matcher m = this.pattern.matcher(lowerCaseText);
        while (m.find()) {
            if (FinderUtils.isWordCompleteInText(m, lowerCaseText)) {
                return Stream.of(BenchmarkResult.of(0, text));
            }
        }
        return Stream.of();
    }
}
