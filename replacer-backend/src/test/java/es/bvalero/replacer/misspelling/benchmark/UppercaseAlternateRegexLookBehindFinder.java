package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UppercaseAlternateRegexLookBehindFinder extends UppercaseAbstractFinder {

    private Pattern words;

    UppercaseAlternateRegexLookBehindFinder(Collection<String> words) {
        String alternations = "(?<=[!#*|=.])\\s*(" + StringUtils.join(words, "|") + ')';
        this.words = Pattern.compile(alternations);
    }

    Set<MatchResult> findMatches(String text) {
        // Build an alternate regex with all the words and match it against the text
        Set<MatchResult> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            String w = m.group().trim();
            int pos = m.group().indexOf(w);
            matches.add(new MatchResult(m.start() + pos, w));
        }
        return matches;
    }

}
