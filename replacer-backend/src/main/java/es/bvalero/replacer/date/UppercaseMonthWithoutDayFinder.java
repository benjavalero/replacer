package es.bvalero.replacer.date;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
class UppercaseMonthWithoutDayFinder extends DateFinder implements ReplacementFinder {

    private static final String SUBTYPE_DATE_UPPERCASE_MONTHS_WITHOUT_DAY = "Mes en mayúscula sin día";

    private static final List<String> WORDS = Arrays.asList(
            "a", "desde", "de", "durante", "el", "entre", "en", "hacia", "hasta", "para", "y");
    private static final List<String> WORDS_UPPERCASE_CLASS = WORDS.stream()
            .map(FinderUtils::setFirstUpperCaseClass)
            .collect(Collectors.toList());

    @RegExp
    private static final String REGEX_DATE_UPPERCASE_MONTHS_WITHOUT_DAY = "(%s) (%s) [Dd]el? <N>{4}";
    private static final RunAutomaton AUTOMATON_DATE_UPPERCASE_MONTHS_WITHOUT_DAY =
            new RunAutomaton(new dk.brics.automaton.RegExp(
                    String.format(REGEX_DATE_UPPERCASE_MONTHS_WITHOUT_DAY,
                            StringUtils.join(WORDS_UPPERCASE_CLASS, "|"),
                            StringUtils.join(MONTHS_UPPERCASE, "|")))
                    .toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<Replacement> findReplacements(String text) {
        return findMatchResults(text, AUTOMATON_DATE_UPPERCASE_MONTHS_WITHOUT_DAY);
    }

    @Override
    public String getSubtype(String text) {
        return SUBTYPE_DATE_UPPERCASE_MONTHS_WITHOUT_DAY;
    }

}
