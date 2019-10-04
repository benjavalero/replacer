package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
class QuotesFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_ANGULAR_QUOTES = "«[^»]+»";
    private static final RunAutomaton AUTOMATON_ANGULAR_QUOTES =
            new RunAutomaton(new RegExp(REGEX_ANGULAR_QUOTES).toAutomaton());

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TYPOGRAPHIC_QUOTES = "“[^”]+”";
    private static final RunAutomaton AUTOMATON_TYPOGRAPHIC_QUOTES =
            new RunAutomaton(new RegExp(REGEX_TYPOGRAPHIC_QUOTES).toAutomaton());

    // For the automaton the quote needs an extra backslash
    private static final String REGEX_DOUBLE_QUOTES = "\\\"[^\\\"\n]+\\\"";
    private static final RunAutomaton AUTOMATON_DOUBLE_QUOTES =
            new RunAutomaton(new RegExp(REGEX_DOUBLE_QUOTES).toAutomaton());

    private static final List<RunAutomaton> AUTOMATA_QUOTES =
            Arrays.asList(AUTOMATON_ANGULAR_QUOTES, AUTOMATON_TYPOGRAPHIC_QUOTES, AUTOMATON_DOUBLE_QUOTES);

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResultsFromAutomata(text, AUTOMATA_QUOTES);
    }

}
