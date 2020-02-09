package es.bvalero.replacer.finder.benchmark;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class WordAlternateRegexCompleteFinder extends WordAbstractFinder {
    private Pattern words;

    WordAlternateRegexCompleteFinder(Collection<String> words) {
        String alternations = "\\b(" + StringUtils.join(words, "|") + ")\\b";
        this.words = Pattern.compile(alternations);
    }

    Set<FinderResult> findMatches(String text) {
        // Build an alternate regex with all the complete words and match it against the text
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            matches.add(FinderResult.of(m.start(), m.group()));
        }
        return matches;
    }
}
