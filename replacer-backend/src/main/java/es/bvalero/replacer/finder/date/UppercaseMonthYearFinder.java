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
 * Find short dates with the month in uppercase, e.g. `Desde Septiembre de 2019`
 */
@Component
public class UppercaseMonthYearFinder extends DateFinder implements ReplacementFinder {
    static final String SUBTYPE_UPPERCASE_MONTH_YEAR = "Mes en mayúscula";

    @RegExp
    private static final String REGEX_UPPERCASE_MONTH_YEAR = "(%s) (%s) ([Dd]el? )?[12]\\.?<N>{3}";

    private static final RunAutomaton AUTOMATON_UPPERCASE_MONTH_YEAR = new RunAutomaton(
        new dk.brics.automaton.RegExp(
            String.format(
                REGEX_UPPERCASE_MONTH_YEAR,
                StringUtils.join(CONNECTORS_UPPERCASE_CLASS, "|"),
                StringUtils.join(MONTHS_UPPERCASE, "|")
            )
        )
        .toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        if (WikipediaLanguage.SPANISH == lang) {
            return new RegexIterable<>(
                text,
                AUTOMATON_UPPERCASE_MONTH_YEAR,
                this::convertMonthYear,
                this::isValidMatch
            );
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getSubtype() {
        return SUBTYPE_UPPERCASE_MONTH_YEAR;
    }
}
