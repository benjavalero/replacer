package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordAlternateRegexFinder extends WordAbstractFinder {

    private Pattern words;

    WordAlternateRegexFinder(Collection<String> words) {
        String alternations = '(' + StringUtils.join(words, "|") + ')';
        this.words = Pattern.compile(alternations);
    }

    Set<IgnoredReplacement> findMatches(String text) {
        // Build an alternate regex with all the words and match it against the text
        Set<IgnoredReplacement> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            if (isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(IgnoredReplacement.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
