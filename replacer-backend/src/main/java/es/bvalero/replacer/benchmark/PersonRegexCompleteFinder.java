package es.bvalero.replacer.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonRegexCompleteFinder extends PersonAbstractFinder {

    private List<Pattern> words;

    PersonRegexCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile(word + ".[A-Z]"));
        }
    }

    Set<IgnoredReplacement> findMatches(String text) {
        // We loop over all the words and find them completely in the text with a regex
        Set<IgnoredReplacement> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                matches.add(IgnoredReplacement.of(m.start(), m.group().substring(0, m.group().length() - 2)));
            }
        }
        return matches;
    }

}
