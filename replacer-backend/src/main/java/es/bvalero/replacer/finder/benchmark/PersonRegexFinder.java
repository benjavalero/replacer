package es.bvalero.replacer.finder.benchmark;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonRegexFinder extends PersonAbstractFinder {

    private List<Pattern> words;

    PersonRegexFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile(word));
        }
    }

    Set<FinderResult> findMatches(String text) {
        // We loop over all the words and find them in the text with a regex
        Set<FinderResult> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                if (isWordFollowedByUppercase(m.start(), m.group(), text)) {
                    matches.add(FinderResult.of(m.start(), m.group()));
                }
            }
        }
        return matches;
    }

}
