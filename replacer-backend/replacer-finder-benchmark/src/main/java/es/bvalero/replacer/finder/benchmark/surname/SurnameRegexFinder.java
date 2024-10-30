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

class SurnameRegexFinder implements BenchmarkFinder {

    private final Collection<Pattern> words = new ArrayList<>();

    SurnameRegexFinder(Collection<String> words) {
        for (String word : words) {
            this.words.add(Pattern.compile(word));
        }
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // We loop over all the words and find them in the text with a regex
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (Pattern word : this.words) {
            final Matcher m = word.matcher(text);
            while (m.find()) {
                if (
                    FinderUtils.isWordCompleteInText(m.start(), m.group(), text) &&
                    FinderUtils.isWordPrecededByUpperCase(m.start(), text)
                ) {
                    matches.add(BenchmarkResult.of(m.start(), m.group()));
                }
            }
        }
        return matches;
    }
}
