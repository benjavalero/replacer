package es.bvalero.replacer.finder.util;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AutomatonMatchFinder {

    public static Iterable<MatchResult> find(String text, RunAutomaton automaton) {
        return () -> new AutomatonIterator(text, automaton);
    }

    private static class AutomatonIterator implements Iterator<MatchResult> {

        private final AutomatonMatcher matcher;

        AutomatonIterator(String text, RunAutomaton automaton) {
            this.matcher = automaton.newMatcher(text);
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
