package es.bvalero.replacer.finder.immutables;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.RegexIterable;
import org.springframework.stereotype.Component;

/**
 * Find XML tags, e. g. `<span>` or `<br />`
 */
@Component
public class XmlTagFinder implements ImmutableFinder {
    // We want to avoid the XML comments to be captured by this
    // For the automaton the < needs an extra backslash
    private static final String REGEX_XML_TAG = "\\</?[A-Za-z][^/\\>]+/?\\>";
    private static final RunAutomaton AUTOMATON_XML_TAG = new RunAutomaton(new RegExp(REGEX_XML_TAG).toAutomaton());

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_XML_TAG, this::convert);
    }
}
