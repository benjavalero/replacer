package es.bvalero.replacer.finder.misspelling.benchmark;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordRegexAlternateFinder extends WordFinder {

    private Pattern words;

    WordRegexAlternateFinder(Collection<String> words) {
        String alternations = '(' + StringUtils.join(words, "|") + ')';
        this.words = Pattern.compile(alternations);
    }

    Set<WordMatch> findWords(String text) {
        // We loop over all the words and find them in the text with the indexOf function
        Set<WordMatch> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            WordMatch match = new WordMatch(m.start(), m.group());
            if (isWordCompleteInText(match, text)) {
                matches.add(match);
            }
        }
        return matches;
    }

}
