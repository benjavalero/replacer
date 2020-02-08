package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find web domains, e. g. `www.acb.es`
 */
@Component
class DomainFinder implements ImmutableFinder {
    private static final String REGEX_DOMAIN = "[A-Za-z.]+\\.[a-z]{2,4}[^A-Za-z]";
    private static final RunAutomaton AUTOMATON_DOMAIN = new RunAutomaton(new RegExp(REGEX_DOMAIN).toAutomaton());

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_DOMAIN, this::convert);
    }

    @Override
    public Immutable convert(MatchResult match) {
        String text = match.group();
        return Immutable.of(match.hashCode(), text.substring(0, text.length() - 1));
    }
}
