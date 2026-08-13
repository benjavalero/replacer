package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Find all the person surnames in the text using a single alternate regex, including the preceding uppercase word.
 * Note: not adaptable to the modern pattern as the condition {@code \p{Lu}\p{L}+} is embedded in the pattern itself,
 * so the match includes the preceding name that must be trimmed from the result.
 */
class SurnameRegexAlternateCompleteFinder implements BenchmarkFinder {

    private final Pattern words;

    SurnameRegexAlternateCompleteFinder(Collection<String> words) {
        String alternations = "\\p{Lu}\\p{L}+ (" + FinderUtils.joinAlternate(words) + ")";
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // Build an alternate regex with all the complete words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final Matcher m = this.words.matcher(text);
        while (m.find()) {
            final int pos = m.group().indexOf(' ') + 1;
            final int start = m.start() + pos;
            final String matchText = m.group().substring(pos);
            if (FinderUtils.isWordCompleteInText(start, matchText, text)) {
                matches.add(BenchmarkResult.of(start, matchText));
            }
        }
        return matches.stream();
    }
}
