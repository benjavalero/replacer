package es.bvalero.replacer.misspelling.benchmark;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordMatchCompleteFinder extends WordFinder {

    private List<Pattern> words;

    WordMatchCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile("\\b" + word + "\\b"));
        }
    }

    Set<WordMatch> findWords(String text) {
        // We loop over all the words and find them in the text with the indexOf function
        Set<WordMatch> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                matches.add(new WordMatch(m.start(), m.group()));
            }
        }
        return matches;
    }

}
