package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
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
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);

        for (ArticleReplacement match : findReplacements(text, AUTOMATON_TEMPLATE_NAME, ReplacementType.IGNORED)) {
            matches.add(match
                    .withStart(match.getStart() + 2)
                    .withText(match.getText().substring(2)));
        }

        return matches;
    }

}
