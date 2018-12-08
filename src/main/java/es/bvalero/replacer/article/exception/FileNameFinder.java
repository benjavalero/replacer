package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FileNameFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_FILE_TAG = "(Archivo|File|Imagen?):[^|\\]\n]+";
    private static final RunAutomaton AUTOMATON_FILE_TAG = new RunAutomaton(new RegExp(REGEX_FILE_TAG).toAutomaton());

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_FILE_NAME =
            "[|=\n][^}|=:\n]+\\.(gif|jpe?g|JPG|mp3|mpg|ogg|ogv|pdf|PDF|png|PNG|svg|tif|webm)";
    private static final RunAutomaton AUTOMATON_FILE_NAME = new RunAutomaton(new RegExp(REGEX_FILE_NAME).toAutomaton());

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);

        for (ArticleReplacement match : ArticleReplacementFinder.findReplacements(text, AUTOMATON_FILE_TAG, ReplacementType.IGNORED)) {
            int posColon = match.getText().indexOf(':') + 1;
            String fileName = match.getText().substring(posColon).trim();
            matches.add(match
                    .withStart(match.getStart() + match.getText().indexOf(fileName))
                    .withText(fileName));
        }

        for (ArticleReplacement match : ArticleReplacementFinder.findReplacements(text, AUTOMATON_FILE_NAME, ReplacementType.IGNORED)) {
            String fileName = match.getText().substring(1).trim();
            matches.add(match
                    .withStart(match.getStart() + match.getText().indexOf(fileName))
                    .withText(fileName));
        }

        return matches;
    }

}
