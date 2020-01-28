package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find template names, e. g. `Bandera` in `{{Bandera|España}}`
 */
@Component
class TemplateNameFinder implements ImmutableFinder {
    private static final String REGEX_TEMPLATE_NAME = "\\{\\{[^|}:]+";
    private static final RunAutomaton AUTOMATON_TEMPLATE_NAME = new RunAutomaton(
        new RegExp(REGEX_TEMPLATE_NAME).toAutomaton()
    );

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<Immutable>(text, AUTOMATON_TEMPLATE_NAME, this::convertMatch, this::isValid);
    }

    private Immutable convertMatch(MatchResult match) {
        // Remove the first 2 characters corresponding to the opening curly braces
        String template = match.group().substring(2).trim();
        int pos = match.group().indexOf(template);
        return Immutable.of(match.start() + pos, template);
    }
}
