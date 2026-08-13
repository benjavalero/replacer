package es.bvalero.replacer.finder.benchmark.surname;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Find all the person surnames in the text using one automaton per word.
 * Then validate that the match is complete in the text and preceded by an uppercase word.
 * Con: iterates over all words independently, so the same position can be visited multiple times.
 */
class SurnameAutomatonFinder implements BenchmarkFinder {

    private final Collection<RunAutomaton> words = new ArrayList<>();

    SurnameAutomatonFinder(Collection<String> words) {
        for (String word : words) {
            this.words.add(new RunAutomaton(new RegExp(word).toAutomaton()));
        }
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return this.words.stream().flatMap(a -> AutomatonMatchFinder.find(page.getContent(), a));
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return (
            FinderUtils.isWordCompleteInText(match, page.getContent()) &&
            FinderUtils.isWordPrecededByUpperCase(match.start(), page.getContent())
        );
    }
}
