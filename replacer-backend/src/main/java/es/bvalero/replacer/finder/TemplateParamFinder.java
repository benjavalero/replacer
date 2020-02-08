package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find template parameters, e. g. `param` in `{{Template|param=value}}`
 */
@Component
class TemplateParamFinder implements ImmutableFinder {
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
