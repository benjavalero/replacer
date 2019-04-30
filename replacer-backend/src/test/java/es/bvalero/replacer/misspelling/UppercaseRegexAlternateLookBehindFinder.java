package es.bvalero.replacer.misspelling;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UppercaseRegexAlternateLookBehindFinder extends WordFinder {

    private Pattern words;

    UppercaseRegexAlternateLookBehindFinder(Collection<String> words) {
        String alternations = "(?<=[!#*|=.])\\s*(" + StringUtils.join(words, "|") + ')';
        this.words = Pattern.compile(alternations);
    }

    Set<WordMatch> findWords(String text) {
        Set<WordMatch> matches = new HashSet<>();

        Matcher m = this.words.matcher(text);
        while (m.find()) {
            String w = m.group().trim();
            int pos = m.group().indexOf(w);
            WordMatch match = new WordMatch(m.start() + pos, w);
            matches.add(match);
        }

        return matches;
    }

}
