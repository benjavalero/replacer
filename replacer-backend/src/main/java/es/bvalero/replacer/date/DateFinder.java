package es.bvalero.replacer.date;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.*;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DateFinder extends ReplacementFinder implements ArticleReplacementFinder {

    private static final String DATE_TYPE = "Fechas";
    private static final String LONG_DATE_TYPE = "Mes en mayúscula";

    private static final List<String> UPPERCASE_MONTHS = Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");
    @RegExp
    private static final String REGEX_LONG_DATE = "(3[01]|[12]<N>|<N>) de (%s) de <N>{4}";
    private static final RunAutomaton AUTOMATON_LONG_DATE = new RunAutomaton(new dk.brics.automaton.RegExp(
            String.format(REGEX_LONG_DATE, StringUtils.join(UPPERCASE_MONTHS, "|")))
            .toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<ArticleReplacement> findReplacements(String text) {
        return findLongDates(text).stream().map(match -> new ArticleReplacement(
                match.getText(),
                match.getStart(),
                getType(),
                LONG_DATE_TYPE,
                Collections.singletonList(new ReplacementSuggestion(match.getText().toLowerCase(), null))))
                .collect(Collectors.toList());
    }

    private List<MatchResult> findLongDates(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        AutomatonMatcher m = AUTOMATON_LONG_DATE.newMatcher(text);
        while (m.find()) {
            if (isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(new MatchResult(m.start(), m.group()));
            }
        }
        return matches;
    }

    @Override
    public String getType() {
        return DATE_TYPE;
    }
}
