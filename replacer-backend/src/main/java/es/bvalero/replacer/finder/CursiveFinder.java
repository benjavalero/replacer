package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CursiveFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    // There is almost no difference with the simple version and the one including the bold (at least with the automaton)
    // so we use the second one which is better
    private static final String BOLD_TEMPLATE = "'{3,}[^']+{3,}";
    @org.intellij.lang.annotations.RegExp
    private static final String TWO_QUOTES_ONLY = "[^']''[^']";
    @org.intellij.lang.annotations.RegExp
    private static final String CURSIVE_REGEX = "%s(('''''|'''|')?[^'\n])*(%s|\n)";
    private static final RunAutomaton CURSIVE_AUTOMATON = new RunAutomaton(new RegExp(
            String.format(CURSIVE_REGEX, TWO_QUOTES_ONLY, TWO_QUOTES_ONLY)).toAutomaton());

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        AutomatonMatcher matcher = CURSIVE_AUTOMATON.newMatcher(text);
        while (matcher.find()) {
            int end = matcher.group().endsWith("\n") ? matcher.group().length() : matcher.group().length() - 1;
            String match = matcher.group().substring(1, end);
            matches.add(new MatchResult(matcher.start() + 1, match));
        }
        return matches;
    }

}
