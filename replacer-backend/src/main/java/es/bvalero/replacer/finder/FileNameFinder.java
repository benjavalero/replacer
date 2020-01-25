package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import es.bvalero.replacer.finder2.RegexIterable;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find filenames, e. g. `xx.jpg` in `[[File:xx.jpg]]`
 */
@Component
class FileNameFinder implements ImmutableFinder {
    // With this regex we also capture domains like www.google.com
    private static final String REGEX_FILE_TAG = "[:=|\n] *[^]:=|/\n]+\\.[A-Za-z]{2,4} *[]}{|\n]";
    private static final RunAutomaton AUTOMATON_FILE_TAG = new RunAutomaton(new RegExp(REGEX_FILE_TAG).toAutomaton());

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<Immutable>(text, AUTOMATON_FILE_TAG, this::convertMatch, this::isValid);
    }

    private Immutable convertMatch(MatchResult match) {
        // Remove the first and last characters and the possible surrounding spaces
        String text = match.group();
        String file = text.substring(1, text.length() - 1).trim();
        int pos = text.indexOf(file);
        return Immutable.of(match.start() + pos, file);
    }
}
