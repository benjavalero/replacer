package es.bvalero.replacer.date;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find long dates starting with zero, e. g. `02 de septiembre de 2019`
 */
@Component
class LeadingZeroFinder extends DateFinder implements ReplacementFinder {
    private static final String SUBTYPE_DATE_LEADING_ZERO = "Día con cero";

    private static final String REGEX_DATE_LEADING_ZERO = "0<N> [Dd]e (%s) [Dd]el? <N>{4}";
    private static final RunAutomaton AUTOMATON_DATE_LEADING_ZERO = new RunAutomaton(
        new dk.brics.automaton.RegExp(
            String.format(REGEX_DATE_LEADING_ZERO, StringUtils.join(MONTHS_UPPERCASE_CLASS, "|"))
        )
        .toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Replacement> find(String text) {
        return new RegexIterable<Replacement>(
            text,
            AUTOMATON_DATE_LEADING_ZERO,
            this::convertMatch,
            this::isValidMatch
        );
    }

    @Override
    public String getSubtype() {
        return SUBTYPE_DATE_LEADING_ZERO;
    }
}
