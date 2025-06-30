package es.bvalero.replacer.finder.benchmark.word;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.regex.MatchResult;

/**
 * Find all the words/expressions in the text with a regex.
 * The regex contains the alternation of all the words/expressions.
 * Then the result is checked to be complete in the text.
 * There is no "complete" version as the automaton doesn't allow complex regex.
 */
class WordAutomatonAlternateFinder implements BenchmarkFinder {

    private final RunAutomaton automaton;

    WordAutomatonAlternateFinder(Collection<String> words) {
        Iterable<String> cleanWords = words.stream().map(this::cleanWord).toList();
        String alternations = FinderUtils.joinAlternate(cleanWords);
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), this.automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
