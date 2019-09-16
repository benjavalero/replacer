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
public class LeadingZeroFinder extends DateFinder implements ArticleReplacementFinder {

    private static final String SUBTYPE_DATE_LEADING_ZERO = "Día con cero";

    @RegExp
    private static final String REGEX_DATE_LEADING_ZERO = "0<N> de (%s) de <N>{4}";
    private static final RunAutomaton AUTOMATON_DATE_LEADING_ZERO = new RunAutomaton(new dk.brics.automaton.RegExp(
            String.format(REGEX_DATE_LEADING_ZERO, StringUtils.join(MONTHS_UPPERCASE_CLASS, "|")))
            .toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<ArticleReplacement> findReplacements(String text) {
        return findMatchResults(text, AUTOMATON_DATE_LEADING_ZERO).stream()
                .filter(match -> isWordCompleteInText(match.getStart(), match.getText(), text))
                .map(match -> convertMatchResultToReplacement(
                        match,
                        TYPE_DATE,
                        SUBTYPE_DATE_LEADING_ZERO,
                        findSuggestions(match.getText())))
                .collect(Collectors.toList());
    }

}
