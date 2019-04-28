package es.bvalero.replacer.misspelling;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class WordIndexOfFinder extends WordFinder {

    private Collection<String> words;

    WordIndexOfFinder(Collection<String> words) {
        this.words = words;
    }

    Set<WordMatch> findWords(String text) {
        // We loop over all the words and find them in the text with the indexOf function
        Set<WordMatch> matches = new HashSet<>();
        for (String word : this.words) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(word, start);
                if (start >= 0) {
                    WordMatch match = new WordMatch(start, word);
                    if (isWordCompleteInText(match, text)) {
                        matches.add(match);
                    }
                    start++;
                }
            }
        }
        return matches;
    }

}
