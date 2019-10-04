package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DomainFinder extends BaseReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_DOMAIN = "[A-Za-z.]+\\.[a-z]{2,4}[^A-Za-z]";
    private static final RunAutomaton AUTOMATON_DOMAIN = new RunAutomaton(new RegExp(REGEX_DOMAIN).toAutomaton());

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_DOMAIN).stream()
                .map(this::processMatchResult)
                .collect(Collectors.toList());
    }

    private MatchResult processMatchResult(MatchResult match) {
        // Remove the last character
        return MatchResult.of(match.getStart(), match.getText().substring(0, match.getText().length() - 1));
    }

}
