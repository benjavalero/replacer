package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class XmlTagFinder implements ExceptionMatchFinder {

    // We want to avoid the XML comments to be captured by this
    private static final RunAutomaton AUTOMATON_XML_TAG =
            new RunAutomaton(new RegExp("\\</?[A-Za-z](<L>|<N>|[ =\"_-])+/?\\>").toAutomaton(new DatatypesAutomatonProvider()));

    private static final Pattern REGEX_XML_TAG_ESCAPED = Pattern.compile("&lt;/?[A-z][\\p{L}\\p{N} =&;_-]+?/?&gt;");

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        if (isTextEscaped) {
            return RegExUtils.findMatches(text, REGEX_XML_TAG_ESCAPED);
        } else {
            return RegExUtils.findMatchesAutomaton(text, AUTOMATON_XML_TAG);
        }
    }

}
