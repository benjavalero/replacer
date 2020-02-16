package es.bvalero.replacer.finder.immutable;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.RegexIterable;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find template names, e. g. `Bandera` in `{{Bandera|España}}`
 */
@Component
public class TemplateNameFinder implements ImmutableFinder {
    private static final String REGEX_TEMPLATE_NAME = "\\{\\{[^|}:]+";
    private static final RunAutomaton AUTOMATON_TEMPLATE_NAME = new RunAutomaton(
        new RegExp(REGEX_TEMPLATE_NAME).toAutomaton()
    );

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_TEMPLATE_NAME, this::convert);
    }

    @Override
    public Immutable convert(MatchResult match) {
        // Remove the first 2 characters corresponding to the opening curly braces
        String template = match.group().substring(2).trim();
        int pos = match.group().indexOf(template);
        return Immutable.of(match.start() + pos, template);
    }
}
