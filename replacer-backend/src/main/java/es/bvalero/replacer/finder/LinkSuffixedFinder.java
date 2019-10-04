package es.bvalero.replacer.finder;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LinkSuffixedFinder extends BaseReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_LINK_SUFFIXED = "\\[\\[[^]]+]]<Ll>+";
    private static final RunAutomaton AUTOMATON_LINK_SUFFIXED =
            new RunAutomaton(new RegExp(REGEX_LINK_SUFFIXED).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_LINK_SUFFIXED);
    }

}
