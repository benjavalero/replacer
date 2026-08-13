package es.bvalero.replacer.finder.benchmark.surname;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Find all the person surnames in the text using a single alternate automaton.
 * Then validate that the match is complete in the text and preceded by an uppercase word.
 */
class SurnameAutomatonAlternateFinder implements BenchmarkFinder {

    private final RunAutomaton words;

    SurnameAutomatonAlternateFinder(Collection<String> words) {
        final String alternations = '(' + FinderUtils.joinAlternate(words) + ')';
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), this.words);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return (
            FinderUtils.isWordCompleteInText(match, page.getContent()) &&
            FinderUtils.isWordPrecededByUpperCase(match.start(), page.getContent())
        );
    }
}
