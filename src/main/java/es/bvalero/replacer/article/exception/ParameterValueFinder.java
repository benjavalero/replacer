package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ParameterValueFinder implements IgnoredReplacementFinder {

    // Look-ahead takes more time
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_INDEX_VALUE = "\\|<Z>*(índice|index|cita|location|ubicación)<Z>*=[^}|]+";
    private static final RunAutomaton AUTOMATON_INDEX_VALUE =
            new RunAutomaton(new RegExp(REGEX_INDEX_VALUE).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);

        for (ArticleReplacement match : ArticleReplacementFinder.findReplacements(text, AUTOMATON_INDEX_VALUE, ReplacementType.IGNORED)) {
            int posEquals = match.getText().indexOf('=') + 1;
            String fileName = match.getText().substring(posEquals).trim();
            matches.add(match
                    .withStart(match.getStart() + match.getText().indexOf(fileName))
                    .withText(fileName));
        }

        return matches;
    }

}
