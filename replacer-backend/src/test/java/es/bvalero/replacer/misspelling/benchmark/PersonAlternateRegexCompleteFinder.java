package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonAlternateRegexCompleteFinder extends PersonAbstractFinder {

    private Pattern words;

    PersonAlternateRegexCompleteFinder(Collection<String> words) {
        String alternations = "(" + StringUtils.join(words, "|") + ").[A-Z]";
        this.words = Pattern.compile(alternations);
    }

    Set<MatchResult> findMatches(String text) {
        // Build an alternate regex with all the complete words and match it against the text
        Set<MatchResult> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            matches.add(new MatchResult(m.start(), m.group().substring(0, m.group().length() - 2)));
        }
        return matches;
    }

}
