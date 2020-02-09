package es.bvalero.replacer.finder.benchmark;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class WordIndexOfFinder extends WordAbstractFinder {
    private Collection<String> words;

    WordIndexOfFinder(Collection<String> words) {
        this.words = words;
    }

    Set<FinderResult> findMatches(String text) {
        // We loop over all the words and find them in the text with the indexOf function
        Set<FinderResult> matches = new HashSet<>();
        for (String word : this.words) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(word, start);
                if (start >= 0) {
                    if (isWordCompleteInText(start, word, text)) {
                        matches.add(FinderResult.of(start, word));
                    }
                    start++;
                }
            }
        }
        return matches;
    }
}
