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
public class ProperNounFinder implements ExceptionMatchFinder {

    private static final RunAutomaton AUTOMATON_PROPER_NOUN =
            new RunAutomaton(new RegExp("(Domingo|Julio)<Z><Lu>").toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        // If we use the automaton, we don't need the extra letter captured for the surname
        List<RegexMatch> nounMatches = RegExUtils.findMatchesAutomaton(text, AUTOMATON_PROPER_NOUN);
        List<RegexMatch> matches = new ArrayList<>();
        for (RegexMatch match : nounMatches) {
            match.setOriginalText(match.getOriginalText().substring(0, match.getOriginalText().length() - 2));
            matches.add(match);
        }
        return matches;
    }

}
