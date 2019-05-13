package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import es.bvalero.replacer.finder.ReplacementFinder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FileNameFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_FILE_TAG = "(Archivo|File|Imagen?):[^|\\]\n]+";
    private static final RunAutomaton AUTOMATON_FILE_TAG = new RunAutomaton(new RegExp(REGEX_FILE_TAG).toAutomaton());

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_FILE_NAME =
            "[|=\n][^}|=:\n]+\\.(gif|jpe?g|JPG|mp3|mpg|ogg|ogv|pdf|PDF|png|PNG|svg|tif|webm)";
    private static final RunAutomaton AUTOMATON_FILE_NAME = new RunAutomaton(new RegExp(REGEX_FILE_NAME).toAutomaton());

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>(100);

        for (MatchResult match : findMatchResults(text, AUTOMATON_FILE_TAG)) {
            int posColon = match.getText().indexOf(':') + 1;
            String fileName = match.getText().substring(posColon).trim();
            matches.add(new MatchResult(match.getStart() + match.getText().indexOf(fileName), fileName));
        }

        for (MatchResult match : findMatchResults(text, AUTOMATON_FILE_NAME)) {
            String fileName = match.getText().substring(1).trim();
            matches.add(new MatchResult(match.getStart() + match.getText().indexOf(fileName), fileName));
        }

        return matches;
    }

}
