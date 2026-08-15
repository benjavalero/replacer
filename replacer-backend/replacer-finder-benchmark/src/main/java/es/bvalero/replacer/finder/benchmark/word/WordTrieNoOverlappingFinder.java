package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.ahocorasick.trie.Trie;

/**
 * Finds the words/expressions in the text using the Aho-Corasick algorithm (Trie library).
 * Then the result is checked to be complete in the text.
 * This approach matches the left-most longest non-overlapping occurrences of keywords.
 */
class WordTrieNoOverlappingFinder implements BenchmarkFinder {

    private final Trie trie;

    WordTrieNoOverlappingFinder(Collection<String> words) {
        this.trie = Trie.builder().ignoreOverlaps().addKeywords(words).build();
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final String text = page.getContent();
        return this.trie.parseText(text)
            .stream()
            .map(emit -> FinderMatchRange.of(text, emit.getStart(), emit.getKeyword()));
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match, page.getContent());
    }
}
