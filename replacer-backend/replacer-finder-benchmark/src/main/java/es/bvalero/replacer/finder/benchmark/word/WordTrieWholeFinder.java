package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.ahocorasick.trie.Trie;

/**
 * Finds the words/expressions in the text using the Aho-Corasick algorithm.
 * Then it checks the results to be complete in the text.
 * This approach matches only whole word matches, i.e., keywords surrounded by non-word characters or string boundaries.
 */
class WordTrieWholeFinder implements BenchmarkFinder {

    private final Trie trie;

    WordTrieWholeFinder(Collection<String> words) {
        this.trie = Trie.builder().onlyWholeWords().addKeywords(words).build();
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return this.trie.parseText(page.getContent())
            .stream()
            .map(emit -> FinderMatchResult.of(emit.getStart(), emit.getKeyword()));
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match, page.getContent());
    }
}
