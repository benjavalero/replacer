package es.bvalero.replacer.finder.date;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Collections;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/**
 * Find long dates with missing prepositions, e.g. `12 agosto 2019`
 */
@Component
public class IncompleteLongDateFinder extends DateFinder implements ReplacementFinder {
    static final String SUBTYPE_INCOMPLETE_LONG_DATE = "Fecha incompleta";

    @RegExp
    private static final String REGEX_INCOMPLETE_LONG_DATE =
        "(3[01]|[12]<N>|<N>) ([Dd]e )?(%s) ([Dd]el? )?[12]\\.?<N>{3}";

    private static final RunAutomaton AUTOMATON_INCOMPLETE_LONG_DATE = new RunAutomaton(
        new dk.brics.automaton.RegExp(
            String.format(REGEX_INCOMPLETE_LONG_DATE, StringUtils.join(MONTHS_LOWERCASE, "|"))
        )
        .toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        if (WikipediaLanguage.SPANISH == lang) {
            return new RegexIterable<>(text, AUTOMATON_INCOMPLETE_LONG_DATE, this::convertLongDate, this::isValidMatch);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isValidMatch(MatchResult match, String text) {
        return ReplacementFinder.super.isValidMatch(match, text) && isMissingPreposition(match.group());
    }

    private boolean isMissingPreposition(String date) {
        int pos1 = date.indexOf("de");
        if (pos1 > 0) {
            int pos2 = date.indexOf("de", pos1 + 1);
            return pos2 <= 0;
        } else {
            return true;
        }
    }

    @Override
    public String getSubtype() {
        return SUBTYPE_INCOMPLETE_LONG_DATE;
    }
}
