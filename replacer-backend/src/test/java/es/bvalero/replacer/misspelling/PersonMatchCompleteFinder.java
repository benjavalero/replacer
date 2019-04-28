package es.bvalero.replacer.misspelling;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonMatchCompleteFinder extends WordFinder {

    private List<Pattern> words;

    PersonMatchCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile(word + ".[A-Z]"));
        }
    }

    Set<WordMatch> findWords(String text) {
        // We loop over all the words and find them in the text with the indexOf function
        Set<WordMatch> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                matches.add(new WordMatch(m.start(), m.group().substring(0, m.group().length() - 2)));
            }
        }
        return matches;
    }

}
