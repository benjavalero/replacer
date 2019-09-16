package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        if (!startsWithLowerCase(word)) {
            throw new IllegalArgumentException(String.format("Word not starting with lowercase: %s", word));
        }
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

    protected static List<MatchResult> findMatchResults(String text, RunAutomaton automaton) {
        List<MatchResult> matches = new ArrayList<>(100);
        AutomatonMatcher matcher = automaton.newMatcher(text);
        while (matcher.find()) {
            matches.add(MatchResult.of(matcher.start(), matcher.group()));
        }
        return matches;
    }

    protected static List<MatchResult> findMatchResults(String text, Pattern pattern) {
        List<MatchResult> matches = new ArrayList<>(100);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matches.add(MatchResult.of(matcher.start(), matcher.group()));
        }
        return matches;
    }

    static List<MatchResult> findMatchResultsFromAutomata(String text, List<RunAutomaton> automata) {
        return automata.stream().map(automaton -> findMatchResults(text, automaton))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    static List<MatchResult> findMatchResultsFromPatterns(String text, List<Pattern> patterns) {
        return patterns.stream().map(pattern -> findMatchResults(text, pattern))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    protected ArticleReplacement convertMatchResultToReplacement(MatchResult match, String type, String subtype,
                                                                 List<ReplacementSuggestion> suggestions) {
        return ArticleReplacement.builder()
                .type(type)
                .subtype(subtype)
                .start(match.getStart())
                .text(match.getText())
                .suggestions(suggestions)
                .build();
    }

}
