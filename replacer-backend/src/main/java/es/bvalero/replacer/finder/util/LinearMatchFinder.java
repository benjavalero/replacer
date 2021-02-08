package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.page.IndexablePage;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LinearMatchFinder {

    public static Iterable<MatchResult> find(IndexablePage page, LinearFinder finder) {
        return () -> new LinearIterator(page, finder);
    }

    private static class LinearIterator implements Iterator<MatchResult> {

        private final IndexablePage page;
        private final LinearFinder finder;
        private int start;
        private MatchResult next;

        LinearIterator(IndexablePage page, LinearFinder finder) {
            this.page = page;
            this.finder = finder;
            this.start = 0;
            this.next = null;
        }

        @Override
        public boolean hasNext() {
            if (start >= page.getContent().length()) {
                return false;
            }
            MatchResult result = null;
            try {
                result = finder.findResult(page, start);
            } catch (Exception e) {
                LOGGER.error("Error finding match result: {} - {}", start, page.getContent(), e);
            }
            if (result == null) {
                next = null;
                start = Integer.MAX_VALUE;
                return false;
            } else {
                next = result;
                start = result.end();
                return true;
            }
        }

        @Override
        public MatchResult next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            return next;
        }
    }
}
