package es.bvalero.replacer.finder.benchmark;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonRegexAllCompleteFinder extends PersonAbstractFinder {

    private Pattern wordPattern;
    private Set<String> words;

    PersonRegexAllCompleteFinder(Collection<String> words) {
        this.wordPattern = Pattern.compile("\\p{Lu}\\p{Ll}++(?=.\\p{Lu})");
        this.words = new HashSet<>(words);
    }

    Set<FinderResult> findMatches(String text) {
        // Find all complete words in the text with a regex and check if they are in the list
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = this.wordPattern.matcher(text);
        while (m.find()) {
            if (this.words.contains(m.group())) {
                matches.add(FinderResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
