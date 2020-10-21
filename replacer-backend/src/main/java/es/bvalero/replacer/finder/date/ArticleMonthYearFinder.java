package es.bvalero.replacer.finder.date;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/**
 * Find short dates which year is preceded by an article, e.g. `Desde agosto del 2019`
 */
@Component
public class ArticleMonthYearFinder extends DateFinder implements ReplacementFinder {
    static final String SUBTYPE_ARTICLE_MONTH_YEAR = "Año con artículo";

    @RegExp
    private static final String REGEX_ARTICLE_MONTH_YEAR = "(%s) (%s) [Dd]el 1<N>{3}";

    private static final RunAutomaton AUTOMATON_ARTICLE_MONTH_YEAR = new RunAutomaton(
        new dk.brics.automaton.RegExp(
            String.format(
                REGEX_ARTICLE_MONTH_YEAR,
                StringUtils.join(CONNECTORS_UPPERCASE_CLASS, "|"),
                StringUtils.join(MONTHS_LOWERCASE, "|")
            )
        )
        .toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        if (WikipediaLanguage.SPANISH == lang) {
            return new RegexIterable<>(text, AUTOMATON_ARTICLE_MONTH_YEAR, this::convertMonthYear, this::isValidMatch);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getSubtype() {
        return SUBTYPE_ARTICLE_MONTH_YEAR;
    }
}
