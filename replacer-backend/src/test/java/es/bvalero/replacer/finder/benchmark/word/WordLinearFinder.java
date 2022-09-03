package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;

class WordLinearFinder implements BenchmarkFinder {

    private final Collection<String> words;

    WordLinearFinder(Collection<String> words) {
        this.words = words;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return IterableUtils.chainedIterable(
            words.stream().map(MisspellingLinearFinder::new).map(finder -> finder.find(page)).toArray(Iterable[]::new)
        );
    }

    private static class MisspellingLinearFinder {

        private final String misspelling;

        MisspellingLinearFinder(String word) {
            this.misspelling = word;
        }

        public Iterable<MatchResult> find(WikipediaPage page) {
            return IterableUtils.toList(LinearMatchFinder.find(page, this::findMisspelling));
        }

        private int findMisspelling(WikipediaPage page, int start, List<MatchResult> matches) {
            final String text = page.getContent();
            final int startMisspelling = findStartMisspelling(text, start);
            if (startMisspelling >= 0) {
                if (FinderUtils.isWordCompleteInText(startMisspelling, misspelling, text)) {
                    matches.add(LinearMatchResult.of(startMisspelling, misspelling));
                }
                return startMisspelling + misspelling.length();
            } else {
                return -1;
            }
        }

        private int findStartMisspelling(String text, int start) {
            return text.indexOf(misspelling, start);
        }
    }
}
