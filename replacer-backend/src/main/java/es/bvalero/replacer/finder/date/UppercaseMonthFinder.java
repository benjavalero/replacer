package es.bvalero.replacer.finder.date;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find dates with the month in uppercase, e.g. `2 de Septiembre de 2019`
 */
@Component
class UppercaseMonthFinder extends DateFinder implements ReplacementFinder {
    private static final String SUBTYPE_DATE_UPPERCASE_MONTHS = "Mes en mayúscula";
    private static final String REGEX_DATE_UPPERCASE_MONTHS = "(3[01]|[12]<N>|<N>) [Dd]e (%s) [Dd]el? <N>{4}";
    private static final RunAutomaton AUTOMATON_DATE_UPPERCASE_MONTHS = new RunAutomaton(
        new dk.brics.automaton.RegExp(
            String.format(REGEX_DATE_UPPERCASE_MONTHS, StringUtils.join(MONTHS_UPPERCASE, "|"))
        )
        .toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        if (WikipediaLanguage.SPANISH == lang) {
            return new RegexIterable<>(text, AUTOMATON_DATE_UPPERCASE_MONTHS, this::convertMatch, this::isValidMatch);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getSubtype() {
        return SUBTYPE_DATE_UPPERCASE_MONTHS;
    }
}
