package es.bvalero.replacer.misspelling;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonIndexOfFinder extends WordFinder {

    private Collection<String> words;

    PersonIndexOfFinder(Collection<String> words) {
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
                    if (isWordFollowedByUppercase(match, text)) {
                        matches.add(match);
                    }
                    start++;
                }
            }
        }
        return matches;
    }

}
