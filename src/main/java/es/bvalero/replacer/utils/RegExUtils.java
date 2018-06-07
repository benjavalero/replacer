package es.bvalero.replacer.utils;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExUtils {

    private RegExUtils() {
    }

    /**
     * Finds the matches for a regular expression in a given text.
     */
    public static List<RegexMatch> findMatches(String text, Pattern pattern) {
        List<RegexMatch> matches = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matches.add(new RegexMatch(matcher.start(), matcher.group(0)));
        }
        return matches;
    }

    public static List<RegexMatch> findMatchesAutomaton(String text, RunAutomaton automaton) {
        List<RegexMatch> matches = new ArrayList<>();
        AutomatonMatcher matcher = automaton.newMatcher(text);
        while (matcher.find()) {
            matches.add(new RegexMatch(matcher.start(), matcher.group(0)));
        }
        return matches;
    }

}
