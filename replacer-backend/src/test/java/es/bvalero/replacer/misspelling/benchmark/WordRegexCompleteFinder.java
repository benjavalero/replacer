package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordRegexCompleteFinder extends WordAbstractFinder {

    private List<Pattern> words;

    WordRegexCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile("\\b" + word + "\\b"));
        }
    }

    Set<MatchResult> findMatches(String text) {
        // We loop over all the words and find them completely in the text with a regex
        Set<MatchResult> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                matches.add(MatchResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
