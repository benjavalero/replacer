package es.bvalero.replacer.finder.immutables;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.ImmutableFinderPriority;
import es.bvalero.replacer.finder.RegexIterable;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find filenames, e. g. `xx.jpg` in `[[File:xx.jpg]]`
 */
@Component
public class FileNameFinder implements ImmutableFinder {
    // With this regex we also capture domains like www.google.com
    private static final String REGEX_FILE_TAG = "[:=|\n] *[^]:=|/\n]+\\.[A-Za-z]{2,4} *[]}{|\n]";
    private static final RunAutomaton AUTOMATON_FILE_TAG = new RunAutomaton(new RegExp(REGEX_FILE_TAG).toAutomaton());

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.MEDIUM;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_FILE_TAG, this::convert);
    }

    @Override
    public Immutable convert(MatchResult match) {
        // Remove the first and last characters and the possible surrounding spaces
        String text = match.group();
        String file = text.substring(1, text.length() - 1).trim();
        int pos = text.indexOf(file);
        return Immutable.of(match.start() + pos, file, this.getClass().getSimpleName());
    }
}
