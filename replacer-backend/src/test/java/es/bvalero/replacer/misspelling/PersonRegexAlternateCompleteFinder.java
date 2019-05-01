package es.bvalero.replacer.misspelling;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonRegexAlternateCompleteFinder extends WordFinder {

    private Pattern words;

    PersonRegexAlternateCompleteFinder(Collection<String> words) {
        String alternations = "(" + StringUtils.join(words, "|") + ").[A-Z]";
        this.words = Pattern.compile(alternations);
    }

    Set<WordMatch> findWords(String text) {
        Set<WordMatch> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            matches.add(new WordMatch(m.start(), m.group().substring(0, m.group().length() - 2)));
        }
        return matches;
    }

}
