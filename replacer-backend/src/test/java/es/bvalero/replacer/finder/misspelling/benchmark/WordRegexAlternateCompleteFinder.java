package es.bvalero.replacer.finder.misspelling.benchmark;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordRegexAlternateCompleteFinder extends WordFinder {

    private Pattern words;

    WordRegexAlternateCompleteFinder(Collection<String> words) {
        String alternations = "\\b(" + StringUtils.join(words, "|") + ")\\b";
        this.words = Pattern.compile(alternations);
    }

    Set<WordMatch> findWords(String text) {
        // We loop over all the words and find them in the text with the indexOf function
        Set<WordMatch> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            matches.add(new WordMatch(m.start(), m.group()));
        }
        return matches;
    }

}
