package es.bvalero.replacer.finder.benchmark.redirection;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class RedirectionAutomatonFinder implements BenchmarkFinder {

    private final RunAutomaton automaton;

    RedirectionAutomatonFinder(Set<String> ignorableTemplates) {
        Set<String> fixedTemplates = ignorableTemplates
            .stream()
            .filter(s -> s.contains("#"))
            .map(s -> s.replace("#", "\\#"))
            .map(FinderUtils::toLowerCase)
            .collect(Collectors.toSet());
        String alternations = '(' + FinderUtils.joinAlternate(fixedTemplates) + ')';
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        final String lowerCaseText = FinderUtils.toLowerCase(text);
        final AutomatonMatcher m = this.automaton.newMatcher(lowerCaseText);
        if (m.find()) {
            return List.of(BenchmarkResult.of(0, text));
        }
        return Collections.emptyList();
    }
}