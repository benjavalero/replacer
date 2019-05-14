package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import es.bvalero.replacer.finder.ReplacementFinder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class SingleQuotesFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_SINGLE_QUOTES_CURSIVE = "[^']''[^']([^'\n]|'''[^'\n]+''')+''[^']";
    private static final RunAutomaton AUTOMATON_SINGLE_QUOTES_CURSIVE =
            new RunAutomaton(new RegExp(REGEX_SINGLE_QUOTES_CURSIVE).toAutomaton());

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_SINGLE_QUOTES_BOLD = "[^']'''[^']([^'\n]|''[^'\n]+'')+'''[^']";
    private static final RunAutomaton AUTOMATON_SINGLE_QUOTES_BOLD =
            new RunAutomaton(new RegExp(REGEX_SINGLE_QUOTES_BOLD).toAutomaton());

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_SINGLE_QUOTES_CURSIVE_BOLD = "'''''[^'\n]+'''''";
    private static final RunAutomaton AUTOMATON_SINGLE_QUOTES_CURSIVE_BOLD =
            new RunAutomaton(new RegExp(REGEX_SINGLE_QUOTES_CURSIVE_BOLD).toAutomaton());

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);

        // For the single-quotes regex, we have to remove the first and last positions found
        Collection<MatchResult> singleQuotesMatches = new ArrayList<>(100);
        singleQuotesMatches.addAll(findMatchResults(text, AUTOMATON_SINGLE_QUOTES_CURSIVE));
        singleQuotesMatches.addAll(findMatchResults(text, AUTOMATON_SINGLE_QUOTES_BOLD));
        for (MatchResult match : singleQuotesMatches) {
            matches.add(new MatchResult(match.getStart() + 1, match.getText().substring(1, match.getText().length() - 1)));
        }

        matches.addAll(findMatchResults(text, AUTOMATON_SINGLE_QUOTES_CURSIVE_BOLD));
        return matches;
    }

}
