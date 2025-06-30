package es.bvalero.replacer.finder.benchmark.word;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;

/**
 * Find all the words in the text using an automaton.
 * Therefore, we can only use it for simple misspellings.
 * Then check if it is a wanted word.
 * Then check if it is complete in the text.
 * There is no "complete" version as the automaton doesn't allow complex regex.
 */
class WordAutomatonAllFinder implements BenchmarkFinder {

    private final RunAutomaton wordAutomaton;
    private final Set<String> words = new HashSet<>();

    WordAutomatonAllFinder(Collection<String> words) {
        this.wordAutomaton = new RunAutomaton(new RegExp("<L>+").toAutomaton(new DatatypesAutomatonProvider()));
        this.words.addAll(words);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), wordAutomaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        final String word = match.group();
        return this.words.contains(word) && FinderUtils.isWordCompleteInText(match.start(), word, page.getContent());
    }
}
