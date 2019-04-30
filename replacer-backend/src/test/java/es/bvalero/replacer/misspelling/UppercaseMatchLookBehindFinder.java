package es.bvalero.replacer.misspelling;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UppercaseMatchLookBehindFinder extends WordFinder {

    private List<Pattern> words;

    UppercaseMatchLookBehindFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile("(?<=[!#*|=.])\\s*" + word));
        }
    }

    Set<WordMatch> findWords(String text) {
        Set<WordMatch> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                String w = m.group().trim();
                int pos = m.group().indexOf(w);
                WordMatch match = new WordMatch(m.start() + pos, w);
                matches.add(match);
            }
        }
        return matches;
    }

}
