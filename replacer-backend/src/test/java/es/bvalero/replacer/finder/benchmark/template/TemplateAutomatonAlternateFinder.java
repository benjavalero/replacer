package es.bvalero.replacer.finder.benchmark.template;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;

class TemplateAutomatonAlternateFinder implements BenchmarkFinder {

    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+}}";
    private static final String REGEX_NESTED = "\\{\\{<Z>*(%s)(<Z>|\n)*[|:](%s|[^}])+}}";
    private final RunAutomaton automaton;

    TemplateAutomatonAlternateFinder(List<String> words) {
        this.automaton =
            new RunAutomaton(
                new RegExp(String.format(REGEX_NESTED, StringUtils.join(toUpperCase(words), '|'), REGEX_TEMPLATE))
                    .toAutomaton(new DatatypesAutomatonProvider())
            );
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), automaton);
    }
}
