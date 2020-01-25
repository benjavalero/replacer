package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import es.bvalero.replacer.finder2.RegexIterable;
import org.springframework.stereotype.Component;

/**
 * Find categories, e. g. `[[Categoría:España]]`
 */
@Component
class CategoryFinder implements ImmutableFinder {
    private static final String REGEX_CATEGORY = "\\[\\[(Categoría|als):[^]]+]]";
    private static final RunAutomaton AUTOMATON_CATEGORY = new RunAutomaton(new RegExp(REGEX_CATEGORY).toAutomaton());

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<Immutable>(text, AUTOMATON_CATEGORY, this::convert, this::isValid);
    }
}
