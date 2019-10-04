package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TemplateParamFinder extends BaseReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE_PARAM = "\\|[^]|=}]+=";
    private static final RunAutomaton AUTOMATON_TEMPLATE_PARAM =
            new RunAutomaton(new RegExp(REGEX_TEMPLATE_PARAM).toAutomaton());

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_TEMPLATE_PARAM).stream()
                .map(this::processMatchResult)
                .collect(Collectors.toList());
    }

    private IgnoredReplacement processMatchResult(IgnoredReplacement match) {
        return IgnoredReplacement.of(match.getStart() + 1, match.getText().substring(1, match.getText().length() - 1));
    }

}
