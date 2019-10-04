package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class TemplateNameFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE_NAME = "\\{\\{[^|}:]+";
    private static final RunAutomaton AUTOMATON_TEMPLATE_NAME =
            new RunAutomaton(new RegExp(REGEX_TEMPLATE_NAME).toAutomaton());

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_TEMPLATE_NAME);
    }

    @Override
    public IgnoredReplacement convertMatch(int start, String text) {
        // Remove the first 2 characters corresponding to the opening curly braces
        return IgnoredReplacement.of(start + 2, text.substring(2));
    }

}
