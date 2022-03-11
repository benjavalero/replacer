package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SurnameRegexCompleteFinder implements BenchmarkFinder {

    private final Collection<Pattern> words = new ArrayList<>();

    SurnameRegexCompleteFinder(Collection<String> words) {
        for (String word : words) {
            this.words.add(Pattern.compile("\\p{Lu}\\p{L}+ " + word));
        }
    }

    @Override
    public Set<BenchmarkResult> findMatches(WikipediaPage page) {
        String text = page.getContent();
        // We loop over all the words and find them completely in the text with a regex
        Set<BenchmarkResult> matches = new HashSet<>();
        for (Pattern word : this.words) {
            final Matcher m = word.matcher(text);
            while (m.find()) {
                final int pos = m.group().indexOf(' ') + 1;
                final int start = m.start() + pos;
                final String matchText = m.group().substring(pos);
                if (FinderUtils.isWordCompleteInText(start, matchText, text)) {
                    matches.add(BenchmarkResult.of(start, matchText));
                }
            }
        }
        return matches;
    }
}
