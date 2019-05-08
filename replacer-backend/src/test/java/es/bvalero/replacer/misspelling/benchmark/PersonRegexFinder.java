package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.finder.MatchResult;

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

    Set<MatchResult> findMatches(String text) {
        // We loop over all the words and find them in the text with a regex
        Set<MatchResult> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                if (isWordFollowedByUppercase(m.start(), m.group(), text)) {
                    matches.add(new MatchResult(m.start(), m.group()));
                }
            }
        }
        return matches;
    }

}
