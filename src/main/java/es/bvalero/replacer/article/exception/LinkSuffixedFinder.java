package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LinkSuffixedFinder implements ExceptionMatchFinder {

    private static final RunAutomaton AUTOMATON_ANGULAR_QUOTES =
            new RunAutomaton(new RegExp("\\[\\[<L>+]]<Ll>+").toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatchesAutomaton(text, AUTOMATON_ANGULAR_QUOTES);
    }

}
