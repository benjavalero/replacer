package es.bvalero.replacer.finder.benchmark.surname;

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
 * Find all the person surnames in the text using a single alternate automaton, including the preceding uppercase word.
 * Note: not adaptable to the modern pattern as the condition {@code <Lu><L>+} is embedded in the pattern itself,
 * so the match includes the preceding name that must be trimmed from the result.
 * This is the same approach used in production ({@link es.bvalero.replacer.finder.immutable.finders.PersonSurnameFinder}).
 */
class SurnameAutomatonAlternateCompleteFinder implements BenchmarkFinder {

    private final RunAutomaton words;

    SurnameAutomatonAlternateCompleteFinder(Collection<String> words) {
        final String alternations = "<Lu><L>+ (" + FinderUtils.joinAlternate(words) + ")";
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // Build an alternate automaton with all the complete words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final AutomatonMatcher m = this.words.newMatcher(text);
        while (m.find()) {
            final int pos = m.group().indexOf(' ') + 1;
            final int start = m.start() + pos;
            final String matchText = m.group().substring(pos);
            if (FinderUtils.isWordCompleteInText(start, matchText, text)) {
                matches.add(BenchmarkResult.of(start, matchText));
            }
        }
        return matches.stream();
    }
}
