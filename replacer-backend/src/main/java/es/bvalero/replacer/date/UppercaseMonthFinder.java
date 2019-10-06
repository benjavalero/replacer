package es.bvalero.replacer.date;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class UppercaseMonthFinder extends DateFinder implements ReplacementFinder {

    private static final String SUBTYPE_DATE_UPPERCASE_MONTHS = "Mes en mayúscula";

    @RegExp
    private static final String REGEX_DATE_UPPERCASE_MONTHS = "(3[01]|[12]<N>|<N>) [Dd]e (%s) [Dd]el? <N>{4}";
    private static final RunAutomaton AUTOMATON_DATE_UPPERCASE_MONTHS = new RunAutomaton(new dk.brics.automaton.RegExp(
            String.format(REGEX_DATE_UPPERCASE_MONTHS, StringUtils.join(MONTHS_UPPERCASE, "|")))
            .toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<Replacement> findReplacements(String text) {
        return findMatchResults(text, AUTOMATON_DATE_UPPERCASE_MONTHS);
    }

    @Override
    public String getSubtype(String text) {
        return SUBTYPE_DATE_UPPERCASE_MONTHS;
    }

}
