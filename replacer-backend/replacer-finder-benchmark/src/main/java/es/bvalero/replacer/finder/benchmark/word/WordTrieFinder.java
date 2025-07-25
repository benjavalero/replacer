package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.regex.MatchResult;
import org.ahocorasick.trie.Trie;

/**
 * Finds the words/expressions in the text using the Aho-Corasick algorithm.
 * Then it checks the results to be complete in the text.
 * This is the simplest approach. It matches all occurrences of all strings, possibly overlapping.
 */
class WordTrieFinder implements BenchmarkFinder {

    private final Trie trie;

    WordTrieFinder(Collection<String> words) {
        this.trie = Trie.builder().addKeywords(words).build();
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return this.trie.parseText(page.getContent())
            .stream()
            .map(emit -> (MatchResult) FinderMatchResult.of(emit.getStart(), emit.getKeyword()))
            .toList();
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
