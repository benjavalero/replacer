package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileNameFinder extends BaseReplacementFinder implements IgnoredReplacementFinder {

    // With this regex we also capture domains like www.google.com
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_FILE_TAG = "[:=|\n] *[^]:=|/\n]+\\.[A-Za-z]{2,4} *[]}{|\n]";
    private static final RunAutomaton AUTOMATON_FILE_TAG = new RunAutomaton(new RegExp(REGEX_FILE_TAG).toAutomaton());

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_FILE_TAG).stream()
                .map(this::processMatchResult)
                .collect(Collectors.toList());
    }

    private IgnoredReplacement processMatchResult(IgnoredReplacement match) {
        // Remove the first and last characters and the possible surrounding spaces
        String text = match.getText();
        String file = text.substring(1, text.length() - 1).trim();
        int pos = text.indexOf(file);
        return IgnoredReplacement.of(match.getStart() + pos, file);
    }

}
