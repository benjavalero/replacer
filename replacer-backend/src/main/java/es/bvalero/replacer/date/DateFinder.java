package es.bvalero.replacer.date;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.*;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DateFinder extends ReplacementFinder implements ArticleReplacementFinder {

    private static final String DATE_TYPE = "Fechas";
    private static final String DATE_UPPERCASE_MONTHS_TYPE = "Mes en mayúscula";
    private static final String DATE_LEADING_ZERO_TYPE = "Día con cero";

    private static final List<String> MONTHS = Arrays.asList(
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "sep?tiembre", "octubre", "noviembre", "diciembre");
    @RegExp
    private static final String REGEX_DATE_UPPERCASE_MONTHS = "(3[01]|[12]<N>|<N>) de (%s) de <N>{4}";
    private static final RunAutomaton AUTOMATON_DATE_UPPERCASE_MONTHS = new RunAutomaton(new dk.brics.automaton.RegExp(
            String.format(REGEX_DATE_UPPERCASE_MONTHS,
                    StringUtils.join(MONTHS.stream().map(ReplacementFinder::setFirstUpperCase).toArray(), "|")))
            .toAutomaton(new DatatypesAutomatonProvider()));

    @RegExp
    private static final String REGEX_DATE_LEADING_ZERO = "0<N> de (%s) de <N>{4}";
    private static final RunAutomaton AUTOMATON_DATE_LEADING_ZERO = new RunAutomaton(new dk.brics.automaton.RegExp(
            String.format(REGEX_DATE_LEADING_ZERO,
                    StringUtils.join(MONTHS.stream().map(ReplacementFinder::setFirstUpperCaseClass).toArray(), "|")))
            .toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<ArticleReplacement> findReplacements(String text) {
        List<ArticleReplacement> replacements = new ArrayList<>(100);

        // Dates with months in uppercase
        findDatesWithUpperCaseMonths(text).forEach(match -> replacements.add(ArticleReplacement.builder()
                .type(getType())
                .subtype(DATE_UPPERCASE_MONTHS_TYPE)
                .start(match.getStart())
                .text(match.getText())
                .suggestions(Collections.singletonList(ReplacementSuggestion.ofNoComment(fixDateWithUpperCaseMonths(match.getText()))))
                .build()));

        // Dates with leading zero
        findDatesWithLeadingZero(text).forEach(match -> replacements.add(ArticleReplacement.builder()
                .type(getType())
                .subtype(DATE_LEADING_ZERO_TYPE)
                .start(match.getStart())
                .text(match.getText())
                .suggestions(Collections.singletonList(ReplacementSuggestion.ofNoComment(fixDateWithLeadingZero(match.getText()))))
                .build()));

        return replacements;
    }

    private List<MatchResult> findDatesWithUpperCaseMonths(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        AutomatonMatcher m = AUTOMATON_DATE_UPPERCASE_MONTHS.newMatcher(text);
        while (m.find()) {
            if (isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(new MatchResult(m.start(), m.group()));
            }
        }
        return matches;
    }

    private String fixDateWithUpperCaseMonths(String date) {
        return date.toLowerCase(Locale.forLanguageTag("es"))
                .replace("setiembre", "septiembre");
    }

    private List<MatchResult> findDatesWithLeadingZero(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        AutomatonMatcher m = AUTOMATON_DATE_LEADING_ZERO.newMatcher(text);
        while (m.find()) {
            if (isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(new MatchResult(m.start(), m.group()));
            }
        }
        return matches;
    }

    private String fixDateWithLeadingZero(String date) {
        return date.toLowerCase(Locale.forLanguageTag("es"))
                .replace("setiembre", "septiembre")
                .substring(1);
    }

    @Override
    public String getType() {
        return DATE_TYPE;
    }
}
