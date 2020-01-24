package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import java.util.Iterator;
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
    public Iterator<Immutable> find(String text) {
        return find(text, AUTOMATON_TEMPLATE_PARAM, this::convertMatch);
    }

    private Immutable convertMatch(MatchResult match) {
        String text = match.group();
        String param = text.substring(1, text.length() - 1).trim();
        int pos = text.indexOf(param);
        return Immutable.of(match.start() + pos, param);
    }
}
