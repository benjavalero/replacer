package es.bvalero.replacer.finder.benchmark.template;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;

class TemplateAutomatonAlternateFinder implements BenchmarkFinder {
    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+}}";
    private static final String REGEX_NESTED = "\\{\\{<Z>*(%s)(<Z>|\n)*[|:](%s|[^}])+}}";
    private RunAutomaton automaton;

    TemplateAutomatonAlternateFinder(List<String> words) {
        this.automaton =
            new RunAutomaton(
                new RegExp(String.format(REGEX_NESTED, StringUtils.join(toUpperCase(words), '|'), REGEX_TEMPLATE))
                .toAutomaton(new DatatypesAutomatonProvider())
            );
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(WikipediaLanguage.getDefault()).build();
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(page, automaton, this::convert)));
    }
}
