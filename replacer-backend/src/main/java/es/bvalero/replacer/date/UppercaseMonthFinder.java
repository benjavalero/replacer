package es.bvalero.replacer.date;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ArticleReplacementFinder;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UppercaseMonthFinder extends DateFinder implements ArticleReplacementFinder {

    @RegExp
    private static final String REGEX_DATE_UPPERCASE_MONTHS = "(3[01]|[12]<N>|<N>) de (%s) del? <N>{4}";
    private static final RunAutomaton AUTOMATON_DATE_UPPERCASE_MONTHS = new RunAutomaton(new dk.brics.automaton.RegExp(
            String.format(REGEX_DATE_UPPERCASE_MONTHS, StringUtils.join(MONTHS_UPPERCASE, "|")))
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
