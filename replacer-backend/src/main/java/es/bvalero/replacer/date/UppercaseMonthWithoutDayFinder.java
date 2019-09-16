package es.bvalero.replacer.date;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ArticleReplacementFinder;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UppercaseMonthWithoutDayFinder extends DateFinder implements ArticleReplacementFinder {

    private static final String SUBTYPE_DATE_UPPERCASE_MONTHS = "Mes en mayúscula";

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
        return findMatchResults(text, AUTOMATON_DATE_UPPERCASE_MONTHS).stream()
                .filter(match -> isWordCompleteInText(match.getStart(), match.getText(), text))
                .map(match -> convertMatchResultToReplacement(
                        match,
                        TYPE_DATE,
                        SUBTYPE_DATE_UPPERCASE_MONTHS,
                        findSuggestions(match.getText())))
                .collect(Collectors.toList());
    }

}
