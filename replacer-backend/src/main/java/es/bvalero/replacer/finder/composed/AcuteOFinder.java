package es.bvalero.replacer.finder.composed;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.misspelling.MisspellingComposedFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/**
 * Find character "ó" between numbers, e.g. `2 ó 3`
 */
@Component
public class AcuteOFinder implements ReplacementFinder {
    static final String SUBTYPE_ACUTE_O = "ó entre números";
    static final String ACUTE_O = "ó";
    static final String FIX_ACUTE_O = "o";

    @RegExp
    private static final String REGEX_ACUTE_O = "<N>+ ó <N>+";

    private static final RunAutomaton AUTOMATON_ACUTE_O = new RunAutomaton(
        new dk.brics.automaton.RegExp(REGEX_ACUTE_O).toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        if (WikipediaLanguage.SPANISH == lang) {
            return new RegexIterable<>(text, AUTOMATON_ACUTE_O, this::convert, this::isValidMatch);
        } else {
            return Collections.emptyList();
        }
    }

    private Replacement convert(MatchResult match) {
        int posSpace = match.group().indexOf(' ');
        int start = match.start() + posSpace + 1;
        return Replacement
            .builder()
            .type(MisspellingComposedFinder.TYPE_MISSPELLING_COMPOSED)
            .subtype(SUBTYPE_ACUTE_O)
            .start(start)
            .text(ACUTE_O)
            .suggestions(findSuggestions())
            .build();
    }

    private List<Suggestion> findSuggestions() {
        return Collections.singletonList(Suggestion.ofNoComment(FIX_ACUTE_O));
    }
}
