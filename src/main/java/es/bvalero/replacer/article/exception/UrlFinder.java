package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UrlFinder implements ExceptionMatchFinder {

    private static final RunAutomaton AUTOMATON_URL =
            new RunAutomaton(new RegExp("https?://<URI>").toAutomaton(new DatatypesAutomatonProvider()));

    private static final String DOMAINS = "(com|org|es|net|gov|edu|gob|info)";
    private static final RunAutomaton AUTOMATON_DOMAIN =
            new RunAutomaton(new RegExp("(<L>+\\.)+" + DOMAINS).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>(100);
        matches.addAll(RegExUtils.findMatchesAutomaton(text, AUTOMATON_URL));
        matches.addAll(RegExUtils.findMatchesAutomaton(text, AUTOMATON_DOMAIN));
        return matches;
    }

}
