package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CursiveFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    // There is almost no difference with the simple version and the one including the bold (at least with the automaton)
    // so we use the second one which is better
    private static final String BOLD_TEMPLATE = "'{3,}[^']+{3,}";
    @org.intellij.lang.annotations.RegExp
    private static final String CURSIVE_REGEX = "''(%s|[^'\n])+(''|\n)";
    private static final RunAutomaton CURSIVE_AUTOMATON
            = new RunAutomaton(new RegExp(String.format(CURSIVE_REGEX, BOLD_TEMPLATE)).toAutomaton());

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResults(text, CURSIVE_AUTOMATON);
    }

}
