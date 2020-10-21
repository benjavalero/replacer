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
 * Find long dates starting with zero, e.g. `02 de septiembre de 2019`
 */
@Component
public class LeadingZeroFinder extends DateFinder implements ReplacementFinder {
    static final String SUBTYPE_LEADING_ZERO = "Día con cero";

    @RegExp
    private static final String REGEX_LEADING_ZERO = "0<N> ([Dd]e )?(%s) ([Dd]el? )?[12]\\.?<N>{3}";

    private static final RunAutomaton AUTOMATON_LEADING_ZERO = new RunAutomaton(
        new dk.brics.automaton.RegExp(String.format(REGEX_LEADING_ZERO, StringUtils.join(MONTHS_LOWERCASE, "|")))
        .toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        if (WikipediaLanguage.SPANISH == lang) {
            return new RegexIterable<>(text, AUTOMATON_LEADING_ZERO, this::convertLongDate, this::isValidMatch);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getSubtype() {
        return SUBTYPE_LEADING_ZERO;
    }
}
