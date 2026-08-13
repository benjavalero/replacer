package es.bvalero.replacer.finder.benchmark.person;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Find all the person names in the text using one automaton per word.
 * Then validate that the match is followed by an uppercase word.
 * Con: iterates over all words independently, so the same position can be visited multiple times.
 */
class PersonAutomatonFinder implements BenchmarkFinder {

    private final List<RunAutomaton> words;

    PersonAutomatonFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(new RunAutomaton(new RegExp(word).toAutomaton()));
        }
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        // We loop over all the words and find them in the text with an automaton
        return this.words.stream().flatMap(a -> AutomatonMatchFinder.find(page.getContent(), a));
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordFollowedByUpperCase(match.start(), match.group(), page.getContent());
    }
}
