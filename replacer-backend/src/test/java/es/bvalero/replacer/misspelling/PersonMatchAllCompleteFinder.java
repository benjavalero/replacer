package es.bvalero.replacer.misspelling;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonMatchAllCompleteFinder extends WordFinder {

    private Pattern wordPattern;
    private Set<String> words;

    PersonMatchAllCompleteFinder(Collection<String> words) {
        this.wordPattern = Pattern.compile("\\p{Lu}\\p{Ll}++(?=.\\p{Lu})");
        this.words = new HashSet<>(words);
    }

    Set<WordMatch> findWords(String text) {
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
