package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.SequencedCollection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LinearMatchCollectionFinder {

    public static Stream<MatchResult> find(FinderPage page, LinearCollectionFinder finder) {
        return ReplacerUtils.streamOfIterable(() -> new LinearIterator(page, finder));
    }

    private static class LinearIterator implements Iterator<MatchResult> {

        private final FinderPage page;
        private final LinearCollectionFinder finder;
        private int start;
        private final Queue<MatchResult> next;

        LinearIterator(FinderPage page, LinearCollectionFinder finder) {
            this.page = page;
            this.finder = finder;
            this.start = 0;
            this.next = new ArrayDeque<>(4);
        }

        @Override
        public boolean hasNext() {
            // This may throw an exception, but it is eventually captured by the indexer.
            // First check if there is a result in the queue
            if (this.next.isEmpty()) {
                final SequencedCollection<MatchResult> result = this.finder.findResults(this.page, this.start);
                if (result.isEmpty()) {
                    this.next.clear();
                    this.start = Integer.MAX_VALUE;
                    return false;
                } else {
                    this.next.addAll(result);
                    this.start = result.getLast().end();
                    return true;
                }
            } else {
                return true;
            }
        }

        @Override
        public MatchResult next() {
            return this.next.remove();
        }
    }
}
