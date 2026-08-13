package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Find all the person names in the text using one regex per word, including the following space and uppercase letter.
 * Con: iterates over all words independently.
 * Note: not adaptable to the modern pattern as the condition {@code \s\p{Lu}} is embedded in the pattern itself,
 * so the match includes 2 extra characters that must be trimmed from the result.
 */
class PersonRegexCompleteFinder implements BenchmarkFinder {

    private final List<Pattern> words;

    PersonRegexCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile(word + "\\s\\p{Lu}"));
        }
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // We loop over all the words and find them completely in the text with a regex
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (Pattern word : this.words) {
            final Matcher m = word.matcher(text);
            while (m.find()) {
                matches.add(BenchmarkResult.of(m.start(), m.group().substring(0, m.group().length() - 2)));
            }
        }
        return matches.stream();
    }
}
