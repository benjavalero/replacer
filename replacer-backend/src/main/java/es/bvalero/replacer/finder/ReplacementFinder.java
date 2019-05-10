package es.bvalero.replacer.finder;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.persistence.ReplacementType;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class to provide generic methods to find replacements
 */
public abstract class ReplacementFinder {

    public List<ArticleReplacement> findReplacements(CharSequence text, RunAutomaton automaton, ReplacementType type) {
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
     * @return If the first letter of the word is uppercase
     */
    protected boolean startsWithUpperCase(CharSequence word) {
        return Character.isUpperCase(word.charAt(0));
    }

}
