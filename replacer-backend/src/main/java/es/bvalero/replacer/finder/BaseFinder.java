package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

interface BaseFinder<T> {

    default List<T> findMatchResults(String text, Pattern pattern) {
        List<T> matches = new ArrayList<>(100);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            if (isValidMatch(matcher.start(), matcher.group(), text)) {
                matches.add(convertMatch(matcher.start(), matcher.group()));
            }
        }
        return matches;
    }

    default List<T> findMatchResultsFromPatterns(String text, List<Pattern> patterns) {
        return patterns.stream().map(pattern -> findMatchResults(text, pattern))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    default List<T> findMatchResults(String text, RunAutomaton automaton) {
        List<T> matches = new ArrayList<>(100);
        AutomatonMatcher matcher = automaton.newMatcher(text);
        while (matcher.find()) {
            if (isValidMatch(matcher.start(), matcher.group(), text)) {
                matches.add(convertMatch(matcher.start(), matcher.group()));
            }
        }
        return matches;
    }

    default List<T> findMatchResultsFromAutomata(String text, List<RunAutomaton> automata) {
        return automata.stream().map(automaton -> findMatchResults(text, automaton))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    boolean isValidMatch(int start, String matchedText, String fullText);



    T convertMatch(int start, String text);

}
