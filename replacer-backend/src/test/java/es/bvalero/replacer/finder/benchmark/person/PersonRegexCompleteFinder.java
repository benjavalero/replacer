package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonRegexCompleteFinder implements BenchmarkFinder {

    private final List<Pattern> words;

    PersonRegexCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile(word + "\\s\\p{Lu}"));
        }
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        // We loop over all the words and find them completely in the text with a regex
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (Pattern word : this.words) {
            final Matcher m = word.matcher(text);
            while (m.find()) {
                matches.add(BenchmarkResult.of(m.start(), m.group().substring(0, m.group().length() - 2)));
            }
        }
        return matches;
    }
}
