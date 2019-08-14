package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TemplateParamFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE_PARAM = "\\|[^|=}]+=";
    private static final RunAutomaton AUTOMATON_TEMPLATE_PARAM =
            new RunAutomaton(new RegExp(REGEX_TEMPLATE_PARAM).toAutomaton());

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        AutomatonMatcher m = AUTOMATON_TEMPLATE_PARAM.newMatcher(text);
        while (m.find()) {
            matches.add(MatchResult.of(m.start() + 1, m.group().substring(1, m.group().length() - 1)));
        }
        return matches;
    }

}
