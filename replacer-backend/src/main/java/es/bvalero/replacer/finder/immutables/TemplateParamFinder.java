package es.bvalero.replacer.finder.immutables;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.RegexIterable;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find template parameters, e. g. `param` in `{{Template|param=value}}`
 */
@Component
public class TemplateParamFinder implements ImmutableFinder {
    // Avoid also ] in case the | comes from a link
    private static final String REGEX_TEMPLATE_PARAM = "\\|[^]|=}]+=";
    private static final RunAutomaton AUTOMATON_TEMPLATE_PARAM = new RunAutomaton(
        new RegExp(REGEX_TEMPLATE_PARAM).toAutomaton()
    );

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_TEMPLATE_PARAM, this::convert);
    }

    @Override
    public Immutable convert(MatchResult match) {
        String text = match.group();
        String param = text.substring(1, text.length() - 1).trim();
        int pos = text.indexOf(param);
        return Immutable.of(match.start() + pos, param);
    }
}
