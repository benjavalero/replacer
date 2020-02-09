package es.bvalero.replacer.finder.benchmark;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonAlternateRegexFinder extends PersonAbstractFinder {

    private Pattern words;

    PersonAlternateRegexFinder(Collection<String> words) {
        String alternations = '(' + StringUtils.join(words, "|") + ')';
        this.words = Pattern.compile(alternations);
    }

    Set<FinderResult> findMatches(String text) {
        // Build an alternate regex with all the words and match it against the text
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            FinderResult match = FinderResult.of(m.start(), m.group());
            if (isWordFollowedByUppercase(m.start(), m.group(), text)) {
                matches.add(match);
            }
        }
        return matches;
    }

}
