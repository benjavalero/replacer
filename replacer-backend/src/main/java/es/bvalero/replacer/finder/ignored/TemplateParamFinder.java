package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.DatatypesAutomatonProvider;
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
public class TemplateParamFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE_PARAM = "\\|<Z>*(<L>|<N>|[ _-])+[<Z>\t]*=";
    private static final RunAutomaton AUTOMATON_TEMPLATE_PARAM =
            new RunAutomaton(new RegExp(REGEX_TEMPLATE_PARAM).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);

        for (ArticleReplacement match : findReplacements(text, AUTOMATON_TEMPLATE_PARAM, ReplacementType.IGNORED)) {
            String param = match.getText().substring(1, match.getText().length() - 1).trim();
            matches.add(match
                    .withStart(match.getStart() + match.getText().indexOf(param))
                    .withText(param));
        }

        return matches;
    }

}
