package es.bvalero.replacer.article;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.persistence.ReplacementType;

import java.util.ArrayList;
import java.util.List;

/**
 * Classes implementing this interface will provide methods to find potential replacements of different types.
 */
@FunctionalInterface
public interface ArticleReplacementFinder {

    static List<ArticleReplacement> findReplacements(CharSequence text, RunAutomaton automaton, ReplacementType type) {
        List<ArticleReplacement> matches = new ArrayList<>(100);
        AutomatonMatcher matcher = automaton.newMatcher(text);
        while (matcher.find()) {
            matches.add(ArticleReplacement.builder()
                    .setStart(matcher.start())
                    .setText(matcher.group(0))
                    .setType(type)
                    .build());
        }
        return matches;
    }

    /**
     * @return A list of potential replacements in the text.
     */
    List<ArticleReplacement> findReplacements(String text);

}
