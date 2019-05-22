package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CursiveFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    private static final String BOLD_TEMPLATE = "'{3,}[^']+{3,}";
    private final static RunAutomaton CURSIVE_AUTOMATON
            = new RunAutomaton(new RegExp(String.format("''(%s|[^'\n])+(''|\n)", BOLD_TEMPLATE)).toAutomaton());

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResults(text, CURSIVE_AUTOMATON);
    }

}
