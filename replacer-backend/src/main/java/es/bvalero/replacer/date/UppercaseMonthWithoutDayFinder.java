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

import java.util.*;
import java.util.stream.Collectors;

@Component
public class UppercaseMonthWithoutDayFinder extends DateFinder implements ArticleReplacementFinder {

    private static final String DATE_UPPERCASE_MONTHS_TYPE = "Mes en mayúscula";

    private static final List<String> WORDS = Arrays.asList(
            "a", "desde", "de", "durante", "el", "entre", "en", "hacia", "hasta", "para", "y");
    private static final List<String> WORDS_UPPERCASE_CLASS = WORDS.stream()
            .map(DateFinder::setFirstUpperCaseClass)
            .collect(Collectors.toList());

    @RegExp
    private static final String REGEX_DATE_UPPERCASE_MONTHS = "(%s) (%s) de <N>{4}";
    private static final RunAutomaton AUTOMATON_DATE_UPPERCASE_MONTHS = new RunAutomaton(new dk.brics.automaton.RegExp(
            String.format(REGEX_DATE_UPPERCASE_MONTHS,
                    StringUtils.join(WORDS_UPPERCASE_CLASS, "|"),
                    StringUtils.join(MONTHS_UPPERCASE, "|")))
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
        return date.substring(0, 1) +
                date.substring(1).toLowerCase(Locale.forLanguageTag("es"))
                        .replace("setiembre", "septiembre");
    }

}
