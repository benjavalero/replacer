package es.bvalero.replacer.finder.immutables;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.ImmutableFinderPriority;
import es.bvalero.replacer.finder.RegexIterable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find filenames, e. g. `xx.jpg` in `[[File:xx.jpg]]`
 */
@Component
public class FileNameFinder implements ImmutableFinder {
    // Files are also found in:
    // - Tag "gallery" ==> Managed in CompleteTagFinder
    // - Template "Gallery" ==> Managed in CompleteTemplateFinder
    // - Parameter values without File prefix ==> To be managed in ParameterValueFinder

    private static final List<String> ALLOWED_PREFIXES = Arrays.asList("Archivo", "File", "Imagen", "Image");
    private static final String REGEX_FILE_TAG = String.format(
        "\\[\\[(%s):[^]|]+",
        StringUtils.join(ALLOWED_PREFIXES, '|')
    );

    private static final RunAutomaton AUTOMATON_FILE_TAG = new RunAutomaton(new RegExp(REGEX_FILE_TAG).toAutomaton());

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.MEDIUM;
    }

    @Override
    public int getMaxLength() {
        return 150;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_FILE_TAG, this::convert);
    }

    @Override
    public Immutable convert(MatchResult match) {
        String text = match.group();
        int filenamePos = text.indexOf(':') + 1;
        return Immutable.of(match.start() + filenamePos, text.substring(filenamePos), this);
    }
}
