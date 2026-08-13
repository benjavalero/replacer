package es.bvalero.replacer.finder.benchmark.person;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Find all the person names in the text using a single alternate automaton, including the following space and uppercase letter.
 * Note: not adaptable to the modern pattern as the condition {@code <Z><Lu>} is embedded in the pattern itself,
 * so the match includes 2 extra characters that must be trimmed from the result.
 */
class PersonAutomatonAlternateCompleteFinder implements BenchmarkFinder {

    private final RunAutomaton words;

    PersonAutomatonAlternateCompleteFinder(Collection<String> words) {
        String alternations = "(" + FinderUtils.joinAlternate(words) + ")<Z><Lu>";
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // Build an alternate automaton with all the complete words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            matches.add(BenchmarkResult.of(m.start(), m.group().substring(0, m.group().length() - 2)));
        }
        return matches.stream();
    }
}
