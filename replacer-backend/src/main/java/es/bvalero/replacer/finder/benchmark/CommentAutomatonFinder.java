package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.HashSet;
import java.util.Set;

class CommentAutomatonFinder extends CommentAbstractFinder {

    private static final RunAutomaton COMMENT_PATTERN
            = new RunAutomaton(new RegExp("\\<!--([^-]|-[^-]|--[^\\>])+--\\>").toAutomaton());

    Set<IgnoredReplacement> findMatches(String text) {
        Set<IgnoredReplacement> matches = new HashSet<>();
        AutomatonMatcher m = COMMENT_PATTERN.newMatcher(text);
        while (m.find()) {
            matches.add(IgnoredReplacement.of(m.start(), m.group()));
        }
        return matches;
    }

}
