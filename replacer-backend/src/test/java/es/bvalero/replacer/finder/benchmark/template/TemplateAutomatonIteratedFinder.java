package es.bvalero.replacer.finder.benchmark.template;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;

class TemplateAutomatonIteratedFinder implements BenchmarkFinder {

    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+}}";
    private static final String REGEX_NESTED = "\\{\\{<Z>*%s(<Z>|\n)*[|:](%s|[^}])+}}";
    private static final List<RunAutomaton> AUTOMATA = new ArrayList<>();

    TemplateAutomatonIteratedFinder(List<String> templateNames) {
        AUTOMATA.addAll(
            toUpperCase(templateNames)
                .stream()
                .map(
                    name ->
                        new RunAutomaton(
                            new RegExp(String.format(REGEX_NESTED, name, REGEX_TEMPLATE))
                                .toAutomaton(new DatatypesAutomatonProvider())
                        )
                )
                .collect(Collectors.toList())
        );
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        Set<BenchmarkResult> matches = new HashSet<>();
        for (RunAutomaton automaton : AUTOMATA) {
            matches.addAll(
                IterableUtils
                    .toList(AutomatonMatchFinder.find(text, automaton))
                    .stream()
                    .map(this::convert)
                    .collect(Collectors.toList())
            );
        }
        return matches;
    }
}
