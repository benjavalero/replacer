package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class to provide generic methods to find replacements
 */
public abstract class ReplacementFinder {

    public List<MatchResult> findMatchResults(CharSequence text, RunAutomaton automaton) {
        List<MatchResult> matches = new ArrayList<>(100);
        AutomatonMatcher matcher = automaton.newMatcher(text);
        while (matcher.find()) {
            matches.add(new MatchResult(matcher.start(), matcher.group(0)));
        }
        return matches;
    }

    public List<MatchResult> findMatchResults(CharSequence text, Pattern pattern) {
        List<MatchResult> matches = new ArrayList<>(100);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matches.add(new MatchResult(matcher.start(), matcher.group()));
        }
        return matches;
    }

    /**
     * @return If the first letter of the word is uppercase
     */
    protected boolean startsWithUpperCase(CharSequence word) {
        return Character.isUpperCase(word.charAt(0));
    }

    protected boolean isWordCompleteInText(int start, String word, String text) {
        int end = start + word.length();
        return start == 0 || end == text.length()
                || (!Character.isLetter(text.charAt(start - 1)) && !Character.isLetter(text.charAt(end)));
    }

}
