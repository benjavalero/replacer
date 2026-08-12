package es.bvalero.replacer.finder.benchmark.word;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Loop over all the words/expressions and find them in the text with an automaton.
 * Then the result is checked to be complete in the text.
 * Con: it iterates over all the words/expressions, so it may be slow for large collections.
 * Note: there is no "complete" version as the automaton doesn't allow complex regex.
 */
class WordAutomatonFinder implements BenchmarkFinder {

    private final List<RunAutomaton> automata;

    WordAutomatonFinder(Collection<String> words) {
        this.automata = new ArrayList<>();
        for (String word : words) {
            this.automata.add(new RunAutomaton(new RegExp(cleanWord(word)).toAutomaton()));
        }
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return automata.stream().flatMap(a -> AutomatonMatchFinder.find(page.getContent(), a));
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match, page.getContent());
    }
}
