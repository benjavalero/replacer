package es.bvalero.replacer.misspelling;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordMatchAllFinder extends WordFinder {

    private Pattern wordPattern;
    private Set<String> words;

    WordMatchAllFinder(Collection<String> words) {
        this.wordPattern = Pattern.compile("[\\w\\-']+", Pattern.UNICODE_CHARACTER_CLASS);
        this.words = new HashSet<>(words);
    }

    Set<WordMatch> findWords(String text) {
        // We loop over all the words and find them in the text with the indexOf function
        Set<WordMatch> matches = new HashSet<>();
        Matcher m = this.wordPattern.matcher(text);
        while (m.find()) {
            WordMatch match = new WordMatch(m.start(), m.group());
            if (this.words.contains(match.getText())) {
                matches.add(match);
            }
        }
        return matches;
    }

}
