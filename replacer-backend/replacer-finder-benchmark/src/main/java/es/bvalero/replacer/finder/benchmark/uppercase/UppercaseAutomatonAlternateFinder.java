package es.bvalero.replacer.finder.benchmark.uppercase;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Build a single alternate automaton with all the words and find them in the text.
 * Then validate that the match is preceded by the expected punctuation.
 * Con: the alternate automaton can be slow for large collections.
 */
class UppercaseAutomatonAlternateFinder extends UppercaseBenchmarkFinder {

    private final RunAutomaton automaton;

    UppercaseAutomatonAlternateFinder(Collection<String> words) {
        this.automaton = new RunAutomaton(
            new RegExp(String.format("(%s)", FinderUtils.joinAlternate(words))).toAutomaton()
        );
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return isWordPrecededByPunctuation(match.start(), page.getContent());
    }
}
