package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InterLanguageLinkFinder extends BaseReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_INTER_LANGUAGE_LINK = "\\[\\[:?[a-z]{2}:[^]]+]]";
    private static final RunAutomaton AUTOMATON_INTER_LANGUAGE_LINK =
            new RunAutomaton(new RegExp(REGEX_INTER_LANGUAGE_LINK).toAutomaton());

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_INTER_LANGUAGE_LINK);
    }

}
