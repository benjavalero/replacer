package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class TemplateParamFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE_PARAM = "\\|[^]|=}]+=";
    private static final RunAutomaton AUTOMATON_TEMPLATE_PARAM =
            new RunAutomaton(new RegExp(REGEX_TEMPLATE_PARAM).toAutomaton());

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_TEMPLATE_PARAM);
    }

    @Override
    public IgnoredReplacement convertMatch(int start, String text) {
        return IgnoredReplacement.of(start + 1, text.substring(1, text.length() - 1));
    }

}
