package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LinkAliasedFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_LINK_ALIASED = "\\[\\[[^]|]+\\|";
    private static final RunAutomaton AUTOMATON_LINK_ALIASED =
            new RunAutomaton(new RegExp(REGEX_LINK_ALIASED).toAutomaton());

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_LINK_ALIASED);
    }

}
