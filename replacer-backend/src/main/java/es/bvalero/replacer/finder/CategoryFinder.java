package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class CategoryFinder extends BaseReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_CATEGORY = "\\[\\[(Categoría|als):[^]]+]]";
    private static final RunAutomaton AUTOMATON_CATEGORY = new RunAutomaton(new RegExp(REGEX_CATEGORY).toAutomaton());

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_CATEGORY);
    }

}
