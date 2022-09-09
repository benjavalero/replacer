package es.bvalero.replacer.finder.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexMatchFinder {

    // As it returns directly the regex matches, we cannot use it in a loop.

    public static Iterable<MatchResult> find(String text, Pattern pattern) {
        return () -> new RegexIterator(text, pattern);
    }

    private static class RegexIterator implements Iterator<MatchResult> {

        private final Matcher matcher;

        RegexIterator(String text, Pattern pattern) {
            this.matcher = pattern.matcher(text);
        }

        @Override
        public boolean hasNext() {
            return matcher.find();
        }

        @Override
        public MatchResult next() {
            if (matcher == null) {
                throw new NoSuchElementException();
            }
            return matcher;
        }
    }
}
