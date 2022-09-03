package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordRegexAllCompleteSeparatorsFinder implements BenchmarkFinder {

    private final Pattern wordPattern;
    private final Set<String> words;

    WordRegexAllCompleteSeparatorsFinder(Collection<String> words) {
        final String leftSeparator = "(?<=^|[^\\w\\d_\\/\\.])";
        final String rightSeparator = "(?=$|[^\\w\\d_\\/])";
        final String regex = leftSeparator + "\\w+" + rightSeparator;
        this.wordPattern = Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
        this.words = new HashSet<>(words);
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        // Find all complete words in the text with a regex and check if they are in the list
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final Matcher m = this.wordPattern.matcher(text);
        while (m.find()) {
            final int start = m.start();
            final String word = m.group();
            if (this.words.contains(word) && !FinderUtils.isApostrophe(text, start - 1)) {
                matches.add(BenchmarkResult.of(start, word));
            }
        }
        return matches;
    }
}
