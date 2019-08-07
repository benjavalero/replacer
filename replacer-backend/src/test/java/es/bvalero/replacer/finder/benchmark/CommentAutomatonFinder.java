package es.bvalero.replacer.finder.benchmark;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.MatchResult;

import java.util.HashSet;
import java.util.Set;

class CommentAutomatonFinder extends CommentAbstractFinder {

    private static final RunAutomaton COMMENT_PATTERN
            = new RunAutomaton(new RegExp("\\<!--([^-]|-[^-]|--[^\\>])+--\\>").toAutomaton());

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        AutomatonMatcher m = COMMENT_PATTERN.newMatcher(text);
        while (m.find()) {
            matches.add(new MatchResult(m.start(), m.group()));
        }
        return matches;
    }

}
