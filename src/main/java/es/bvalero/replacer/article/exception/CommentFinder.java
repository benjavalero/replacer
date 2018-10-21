package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentFinder implements IgnoredReplacementFinder {

    // The nested regex takes twice more but it is worth as it captures completely the templates with inner templates
    // See the CompleteTagFinder to see how the alternative for the nested tags is implemented
    // For the automaton the "<" and ">" need an extra backslash
    @SuppressWarnings("RegExpRedundantEscape")
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_COMMENT_TAG = "\\<!--([^-]|-[^-]|--[^\\>])+--\\>";
    private static final RunAutomaton AUTOMATON_COMMENT_TAG =
            new RunAutomaton(new RegExp(REGEX_COMMENT_TAG).toAutomaton());

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        return ArticleReplacementFinder.findReplacements(text, AUTOMATON_COMMENT_TAG, ReplacementType.IGNORED);
    }

}
