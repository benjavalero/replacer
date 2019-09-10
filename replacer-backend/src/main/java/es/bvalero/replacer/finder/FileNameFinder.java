package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FileNameFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    // With this regex we also capture domains like www.google.com
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_FILE_TAG = "[:=|\n] *[^]:=|/\n]+\\.[A-Za-z]{2,4} *[]}|\n]";
    private static final RunAutomaton AUTOMATON_FILE_TAG
            = new RunAutomaton(new RegExp(REGEX_FILE_TAG).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);

        AutomatonMatcher m = AUTOMATON_FILE_TAG.newMatcher(text);
        while (m.find()) {
            // Remove the first and last characters and the possible surrounding spaces
            String file = m.group().substring(1, m.group().length() - 1).trim();
            int pos = m.group().indexOf(file);
            matches.add(MatchResult.of(m.start() + pos, file));
        }

        return matches;
    }

}
