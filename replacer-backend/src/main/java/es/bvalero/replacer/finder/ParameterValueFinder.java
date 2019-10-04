package es.bvalero.replacer.finder;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParameterValueFinder extends BaseReplacementFinder implements IgnoredReplacementFinder {

    private static final List<String> PARAMS = Arrays.asList("índice", "index", "cita", "species");
    private static final RunAutomaton AUTOMATON_PARAM_VALUE = new RunAutomaton(
            new RegExp(String.format("\\|<Z>*(%s)<Z>*=[^|}]+", StringUtils.join(PARAMS, "|")))
                    .toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_PARAM_VALUE).stream()
                .map(this::processMatchResult)
                .collect(Collectors.toList());
    }

    private MatchResult processMatchResult(MatchResult match) {
        int pos = match.getText().indexOf('=') + 1;
        return MatchResult.of(match.getStart() + pos, match.getText().substring(pos));
    }

}
