package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import es.bvalero.replacer.finder.ReplacementFinder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    // The nested regex takes twice more but it is worth as it captures completely the templates with inner templates
    // See the CompleteTagFinder to see how the alternative for the nested tags is implemented
    // For the automaton the "<" and ">" need an extra backslash
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_COMMENT_TAG = "\\<!--([^-]|-[^-]|--[^\\>])+--\\>";
    private static final RunAutomaton AUTOMATON_COMMENT_TAG =
            new RunAutomaton(new RegExp(REGEX_COMMENT_TAG).toAutomaton());

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResults(text, AUTOMATON_COMMENT_TAG);
    }

}
