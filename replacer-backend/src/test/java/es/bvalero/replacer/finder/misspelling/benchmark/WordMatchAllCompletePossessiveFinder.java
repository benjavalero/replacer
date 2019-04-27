package es.bvalero.replacer.finder.misspelling.benchmark;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordMatchAllCompletePossessiveFinder extends WordFinder {

    private Pattern wordPattern;
    private Set<String> words;

    WordMatchAllCompletePossessiveFinder(Collection<String> words) {
        this.wordPattern = Pattern.compile("\\b[\\w\\-']++\\b", Pattern.UNICODE_CHARACTER_CLASS);
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
