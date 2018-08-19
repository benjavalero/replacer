package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class XmlEntityFinder implements ExceptionMatchFinder {

    private static final RunAutomaton AUTOMATON_XML_ENTITY =
            new RunAutomaton(new RegExp("&[a-z]+?;").toAutomaton());

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        if (isTextEscaped) {
            matches.addAll(RegExUtils.findMatchesAutomaton(text, AUTOMATON_XML_ENTITY));
        }
        return matches;
    }

}
