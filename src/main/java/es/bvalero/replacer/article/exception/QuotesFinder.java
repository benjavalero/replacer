package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class QuotesFinder implements IgnoredReplacementFinder {

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

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_ANGULAR_QUOTES = "«[^»]+»";
    private static final RunAutomaton AUTOMATON_ANGULAR_QUOTES =
            new RunAutomaton(new RegExp(REGEX_ANGULAR_QUOTES).toAutomaton());

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TYPOGRAPHIC_QUOTES = "“[^”]+”";
    private static final RunAutomaton AUTOMATON_TYPOGRAPHIC_QUOTES =
            new RunAutomaton(new RegExp(REGEX_TYPOGRAPHIC_QUOTES).toAutomaton());

    // For the automaton the quote needs an extra backslash
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_DOUBLE_QUOTES = "\\\"[^\\\"\n]+\\\"";
    private static final RunAutomaton AUTOMATON_DOUBLE_QUOTES =
            new RunAutomaton(new RegExp(REGEX_DOUBLE_QUOTES).toAutomaton());

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);

        // For the single-quotes regex, we have to remove the first and last positions found
        Collection<ArticleReplacement> singleQuotesMatches = new ArrayList<>(100);
        singleQuotesMatches.addAll(ArticleReplacementFinder.findReplacements(text, AUTOMATON_SINGLE_QUOTES_CURSIVE, ReplacementType.IGNORED));
        singleQuotesMatches.addAll(ArticleReplacementFinder.findReplacements(text, AUTOMATON_SINGLE_QUOTES_BOLD, ReplacementType.IGNORED));
        for (ArticleReplacement match : singleQuotesMatches) {
            matches.add(match
                    .withStart(match.getStart() + 1)
                    .withText(match.getText().substring(1, match.getText().length() - 1)));
        }

        matches.addAll(ArticleReplacementFinder.findReplacements(text, AUTOMATON_SINGLE_QUOTES_CURSIVE_BOLD, ReplacementType.IGNORED));
        matches.addAll(ArticleReplacementFinder.findReplacements(text, AUTOMATON_DOUBLE_QUOTES, ReplacementType.IGNORED));
        matches.addAll(ArticleReplacementFinder.findReplacements(text, AUTOMATON_ANGULAR_QUOTES, ReplacementType.IGNORED));
        matches.addAll(ArticleReplacementFinder.findReplacements(text, AUTOMATON_TYPOGRAPHIC_QUOTES, ReplacementType.IGNORED));
        return matches;
    }

}
