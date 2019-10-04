package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TemplateNameFinder extends BaseReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE_NAME = "\\{\\{[^|}:]+";
    private static final RunAutomaton AUTOMATON_TEMPLATE_NAME =
            new RunAutomaton(new RegExp(REGEX_TEMPLATE_NAME).toAutomaton());

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_TEMPLATE_NAME).stream()
                .map(this::processMatchResult)
                .collect(Collectors.toList());
    }

    private IgnoredReplacement processMatchResult(IgnoredReplacement match) {
        // Remove the first 2 characters corresponding to the opening curly braces
        return IgnoredReplacement.of(match.getStart() + 2, match.getText().substring(2));
    }

}
