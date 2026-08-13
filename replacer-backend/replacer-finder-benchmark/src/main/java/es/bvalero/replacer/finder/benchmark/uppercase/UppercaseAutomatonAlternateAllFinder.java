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
 * Note: unlike {@link UppercaseAutomatonAlternateFinder}, this one does not use the DatatypesAutomatonProvider,
 * so it cannot use Unicode character classes like {@code <Zs>}.
 */
class UppercaseAutomatonAlternateAllFinder extends UppercaseBenchmarkFinder {

    private final RunAutomaton words;

    UppercaseAutomatonAlternateAllFinder(Collection<String> words) {
        String alternations = String.format("(%s)", FinderUtils.joinAlternate(words));
        this.words = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), words);
    }

    @Override
    public boolean validate(MatchResult matchResult, FinderPage page) {
        return isWordPrecededByPunctuation(matchResult.start(), page.getContent());
    }
}
