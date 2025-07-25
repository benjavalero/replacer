package es.bvalero.replacer.finder.benchmark.redirection;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.List;

class RedirectionAutomatonFinder implements BenchmarkFinder {

    private final RunAutomaton automaton;

    RedirectionAutomatonFinder(List<String> redirectionWords) {
        List<String> fixedRedirectionWords = redirectionWords.stream().map(s -> s.replace("#", "\\#")).toList();
        String alternations = '(' + FinderUtils.joinAlternate(fixedRedirectionWords) + ')';
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final String lowerCaseText = FinderUtils.toLowerCase(text);
        final AutomatonMatcher m = this.automaton.newMatcher(lowerCaseText);
        if (m.find()) {
            return List.of(BenchmarkResult.of(0, text));
        }
        return List.of();
    }
}
