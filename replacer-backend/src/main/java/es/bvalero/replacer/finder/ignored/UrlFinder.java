package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import es.bvalero.replacer.finder.ReplacementFinder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UrlFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_URL = "https?://<URI>";
    private static final RunAutomaton AUTOMATON_URL =
            new RunAutomaton(new RegExp(REGEX_URL).toAutomaton(new DatatypesAutomatonProvider()));

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_DOMAINS = "(<L>+\\.)+(com|org|es|net|gov|edu|gob|info)";
    private static final RunAutomaton AUTOMATON_DOMAIN =
            new RunAutomaton(new RegExp(REGEX_DOMAINS).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);
        matches.addAll(findMatchResults(text, AUTOMATON_URL));
        matches.addAll(findMatchResults(text, AUTOMATON_DOMAIN));
        return matches;
    }

}
