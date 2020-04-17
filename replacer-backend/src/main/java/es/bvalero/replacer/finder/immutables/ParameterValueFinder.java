package es.bvalero.replacer.finder.immutables;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.RegexIterable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find the values of some parameters, e. g. `value` in `{{Template|index=value}}`
 */
@Component
class ParameterValueFinder implements ImmutableFinder {
    private static final List<String> PARAMS = Arrays.asList(
        "cita",
        "imagen<N>?",
        "index",
        "índice",
        "link",
        "species",
        "var"
    );
    private static final String REGEX_PARAM_VALUE = String.format(
        "\\|<Z>*(%s)<Z>*=[^|}]+",
        StringUtils.join(PARAMS, "|")
    );
    private static final RunAutomaton AUTOMATON_PARAM_VALUE = new RunAutomaton(
        new RegExp(REGEX_PARAM_VALUE).toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_PARAM_VALUE, this::convert);
    }

    @Override
    public Immutable convert(MatchResult match) {
        return Immutable.of(match.start() + 1, match.group().substring(1), this);
    }
}
