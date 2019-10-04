package es.bvalero.replacer.finder;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
class ParameterValueFinder implements IgnoredReplacementFinder {

    private static final List<String> PARAMS = Arrays.asList("índice", "index", "cita", "species");
    private static final RunAutomaton AUTOMATON_PARAM_VALUE = new RunAutomaton(
            new RegExp(String.format("\\|<Z>*(%s)<Z>*=[^|}]+", StringUtils.join(PARAMS, "|")))
                    .toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_PARAM_VALUE);
    }

    @Override
    public IgnoredReplacement convertMatch(int start, String text) {
        int pos = text.indexOf('=') + 1;
        return IgnoredReplacement.of(start + pos, text.substring(pos));
    }

}
