package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class WordLinearAllFinder implements BenchmarkFinder {
    private final Set<String> words = new HashSet<>();

    WordLinearAllFinder(Collection<String> words) {
        this.words.addAll(words);
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        int start = 0;
        while (start >= 0) {
            int i = start;
            for (; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (!Character.isLetter(ch) && ch != '-' && ch != '\'') {
                    String word = text.substring(start, i);
                    if (words.contains(word)) {
                        matches.add(FinderResult.of(start, word));
                    }
                    break;
                }
            }
            start = i == text.length() ? -1 : i + 1;
        }
        return matches;
    }
}
