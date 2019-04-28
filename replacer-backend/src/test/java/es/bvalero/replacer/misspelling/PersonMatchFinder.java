package es.bvalero.replacer.misspelling;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonMatchFinder extends WordFinder {

    private List<Pattern> words;

    PersonMatchFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile(word));
        }
    }

    Set<WordMatch> findWords(String text) {
        // We loop over all the words and find them in the text with the indexOf function
        Set<WordMatch> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                WordMatch match = new WordMatch(m.start(), m.group());
                if (isWordFollowedByUppercase(match, text)) {
                    matches.add(match);
                }
            }
        }
        return matches;
    }

}
