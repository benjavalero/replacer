package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ParameterValueFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    private final static List<String> PARAMS = Arrays.asList("índice", "index", "cita");
    private final static RunAutomaton AUTOMATON = new RunAutomaton(
            new RegExp(String.format("\\|<Z>*(%s)<Z>*=[^|}]+", StringUtils.join(PARAMS, "|")))
                    .toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        AutomatonMatcher m = AUTOMATON.newMatcher(text);
        while (m.find()) {
            int pos = m.group().indexOf("=") + 1;
            matches.add(new MatchResult(m.start() + pos, m.group().substring(pos)));
        }
        return matches;
    }

}
