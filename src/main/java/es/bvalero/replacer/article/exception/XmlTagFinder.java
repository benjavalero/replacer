package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XmlTagFinder implements IgnoredReplacementFinder {

    // We want to avoid the XML comments to be captured by this
    // For the automaton the < needs an extra backslash
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_XML_TAG = "\\</?[A-Za-z][^/\\>]+/?\\>";
    private static final RunAutomaton AUTOMATON_XML_TAG =
            new RunAutomaton(new RegExp(REGEX_XML_TAG).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        return ArticleReplacementFinder.findReplacements(text, AUTOMATON_XML_TAG, ReplacementType.IGNORED);
    }

}
