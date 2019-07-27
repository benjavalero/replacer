package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class to provide generic methods to find replacements
 */
public abstract class ReplacementFinder {

    private static final Set<Character> invalidSeparators = new HashSet<>(Arrays.asList('_', '/'));

    protected static boolean startsWithUpperCase(CharSequence word) {
        return Character.isUpperCase(word.charAt(0));
    }

    protected static boolean startsWithLowerCase(CharSequence word) {
        return Character.isLowerCase(word.charAt(0));
    }

    protected static String setFirstUpperCase(String word) {
        return word.substring(0, 1).toUpperCase(Locale.forLanguageTag("es")) + word.substring(1);
    }

    protected static String setFirstUpperCaseClass(String word) {
        return String.format("[%s%s]%s",
                word.substring(0, 1).toUpperCase(Locale.forLanguageTag("es")),
                word.substring(0, 1),
                word.substring(1));
    }

    protected static boolean isWordCompleteInText(int start, String word, String text) {
        int end = start + word.length();
        return start == 0 || end == text.length()
                || (isValidSeparator(text.charAt(start - 1)) && isValidSeparator(text.charAt(end)));
    }

    private static boolean isValidSeparator(char separator) {
        return !Character.isLetterOrDigit(separator) && !invalidSeparators.contains(separator);
    }

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

}
