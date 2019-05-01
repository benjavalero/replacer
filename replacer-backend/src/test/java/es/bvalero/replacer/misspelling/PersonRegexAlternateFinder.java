package es.bvalero.replacer.misspelling;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonRegexAlternateFinder extends WordFinder {

    private Pattern words;

    PersonRegexAlternateFinder(Collection<String> words) {
        String alternations = '(' + StringUtils.join(words, "|") + ')';
        this.words = Pattern.compile(alternations);
    }

    Set<WordMatch> findWords(String text) {
        Set<WordMatch> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            WordMatch match = new WordMatch(m.start(), m.group());
            if (isWordFollowedByUppercase(match, text)) {
                matches.add(match);
            }
        }
        return matches;
    }

}
