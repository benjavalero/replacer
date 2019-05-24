package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TemplateNameFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE_NAME = "\\{\\{[^|}:]+";
    private static final RunAutomaton AUTOMATON_TEMPLATE_NAME =
            new RunAutomaton(new RegExp(REGEX_TEMPLATE_NAME).toAutomaton());

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        AutomatonMatcher m = AUTOMATON_TEMPLATE_NAME.newMatcher(text);
        while (m.find()) {
            // Remove the first 2 characters corresponding to the opening curly braces
            matches.add(new MatchResult(m.start() + 2, m.group().substring(2)));
        }
        return matches;
    }

}
