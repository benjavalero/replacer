package es.bvalero.replacer.finder.date;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find months in uppercase without day and after a common preposition, e. g. `desde Septiembre de 2019`
 */
@Component
class UppercaseMonthWithoutDayFinder extends DateFinder implements ReplacementFinder {
    private static final String SUBTYPE_DATE_UPPERCASE_MONTHS_WITHOUT_DAY = "Mes en mayúscula sin día";
    private static final List<String> WORDS = Arrays.asList(
        "a",
        "desde",
        "de",
        "durante",
        "el",
        "entre",
        "en",
        "hacia",
        "hasta",
        "para",
        "y"
    );
    private static final List<String> WORDS_UPPERCASE_CLASS = WORDS
        .stream()
        .map(FinderUtils::setFirstUpperCaseClass)
        .collect(Collectors.toList());
    private static final String REGEX_DATE_UPPERCASE_MONTHS_WITHOUT_DAY = "(%s) (%s) [Dd]el? <N>{4}";
    private static final RunAutomaton AUTOMATON_DATE_UPPERCASE_MONTHS_WITHOUT_DAY = new RunAutomaton(
        new dk.brics.automaton.RegExp(
            String.format(
                REGEX_DATE_UPPERCASE_MONTHS_WITHOUT_DAY,
                StringUtils.join(WORDS_UPPERCASE_CLASS, "|"),
                StringUtils.join(MONTHS_UPPERCASE, "|")
            )
        )
        .toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Replacement> find(String text) {
        return new RegexIterable<>(
            text,
            AUTOMATON_DATE_UPPERCASE_MONTHS_WITHOUT_DAY,
            this::convertMatch,
            this::isValidMatch
        );
    }

    @Override
    public String getSubtype() {
        return SUBTYPE_DATE_UPPERCASE_MONTHS_WITHOUT_DAY;
    }
}
