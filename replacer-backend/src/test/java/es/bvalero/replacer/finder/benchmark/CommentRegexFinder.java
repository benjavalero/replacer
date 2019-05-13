package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CommentRegexFinder extends CommentAbstractFinder {

    private final static Pattern COMMENT_PATTERN = Pattern.compile("<!--.+?-->", Pattern.DOTALL);

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        Matcher m = COMMENT_PATTERN.matcher(text);
        while (m.find()) {
            matches.add(new MatchResult(m.start(), m.group()));
        }
        return matches;
    }

}
