package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FileNameFinder implements ExceptionMatchFinder {

    private static final RunAutomaton AUTOMATON_FILE_TAG
            = new RunAutomaton(new RegExp("(Archivo|File|Imagen):[^\\|\\]\n]+").toAutomaton());

    private static final String REGEX_FILE_NAME =
            "([|=\n])[^}|=:\n]+\\.(gif|jpe?g|JPG|mp3|mpg|ogg|ogv|pdf|PDF|png|PNG|svg|tif|webm)";
    private static final RunAutomaton AUTOMATON_FILE_NAME = new RunAutomaton(new RegExp(REGEX_FILE_NAME).toAutomaton());

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>(100);
        matches.addAll(RegExUtils.findMatchesAutomaton(text, AUTOMATON_FILE_TAG));
        matches.addAll(RegExUtils.findMatchesAutomaton(text, AUTOMATON_FILE_NAME));
        return matches;
    }

}
