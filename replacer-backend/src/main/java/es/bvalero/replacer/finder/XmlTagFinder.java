package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class XmlTagFinder implements IgnoredReplacementFinder {

    // We want to avoid the XML comments to be captured by this
    // For the automaton the < needs an extra backslash
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_XML_TAG = "\\</?[A-Za-z][^/\\>]+/?\\>";
    private static final RunAutomaton AUTOMATON_XML_TAG = new RunAutomaton(new RegExp(REGEX_XML_TAG).toAutomaton());

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_XML_TAG);
    }

}
