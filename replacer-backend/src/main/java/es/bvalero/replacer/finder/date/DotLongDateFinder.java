package es.bvalero.replacer.finder.date;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/**
 * Find long dates which year has a dot, e.g. `12 de agosto de 2.019`
 */
@Component
public class DotLongDateFinder extends DateFinder implements ReplacementFinder {
    static final String SUBTYPE_DOT_LONG_DATE = "Año con punto";

    @RegExp
    private static final String REGEX_DOT_LONG_DATE = "(3[01]|[12]<N>|<N>) [Dd]e (%s) [Dd]el? [12]\\.<N>{3}";

    private static final RunAutomaton AUTOMATON_DOT_LONG_DATE = new RunAutomaton(
        new dk.brics.automaton.RegExp(String.format(REGEX_DOT_LONG_DATE, StringUtils.join(MONTHS_LOWERCASE, "|")))
        .toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        if (WikipediaLanguage.SPANISH == lang) {
            return new RegexIterable<>(text, AUTOMATON_DOT_LONG_DATE, this::convertLongDate, this::isValidMatch);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getSubtype() {
        return SUBTYPE_DOT_LONG_DATE;
    }
}
