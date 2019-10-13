package es.bvalero.replacer.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UppercaseRegexFinder extends UppercaseAbstractFinder {

    private List<Pattern> words;

    UppercaseRegexFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile("[!#*|=.]\\s*(" + word + ")"));
        }
    }

    Set<IgnoredReplacement> findMatches(String text) {
        // We loop over all the words and find them in the text with a regex
        Set<IgnoredReplacement> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                String w = m.group(1);
                int pos = m.group().indexOf(w);
                matches.add(IgnoredReplacement.of(m.start() + pos, w));
            }
        }
        return matches;
    }

}
