package es.bvalero.replacer.date;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ArticleReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import es.bvalero.replacer.finder.ReplacementSuggestion;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class UppercaseMonthFinder extends DateFinder implements ArticleReplacementFinder {

    private static final String DATE_UPPERCASE_MONTHS_TYPE = "Mes en mayúscula";

    @RegExp
    private static final String REGEX_DATE_UPPERCASE_MONTHS = "(3[01]|[12]<N>|<N>) de (%s) de <N>{4}";
    private static final RunAutomaton AUTOMATON_DATE_UPPERCASE_MONTHS = new RunAutomaton(new dk.brics.automaton.RegExp(
            String.format(REGEX_DATE_UPPERCASE_MONTHS, StringUtils.join(MONTHS_UPPERCASE, "|")))
            .toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<ArticleReplacement> findReplacements(String text) {
        return findDatesWithUpperCaseMonths(text).stream()
                .map(this::convertMatchToReplacement)
                .collect(Collectors.toList());
    }

    private List<MatchResult> findDatesWithUpperCaseMonths(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        AutomatonMatcher m = AUTOMATON_DATE_UPPERCASE_MONTHS.newMatcher(text);
        while (m.find()) {
            if (isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(MatchResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }

    private ArticleReplacement convertMatchToReplacement(MatchResult match) {
        return ArticleReplacement.builder()
                .type(getType())
                .subtype(DATE_UPPERCASE_MONTHS_TYPE)
                .start(match.getStart())
                .text(match.getText())
                .suggestions(Collections.singletonList(ReplacementSuggestion.ofNoComment(fixDateWithUpperCaseMonth(match.getText()))))
                .build();
    }

    private String fixDateWithUpperCaseMonth(String date) {
        return date.toLowerCase(Locale.forLanguageTag("es"))
                .replace("setiembre", "septiembre");
    }

}
