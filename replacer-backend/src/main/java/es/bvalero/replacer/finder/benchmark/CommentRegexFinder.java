package es.bvalero.replacer.finder.benchmark;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CommentRegexFinder extends CommentAbstractFinder {
    private static final Pattern COMMENT_PATTERN = Pattern.compile("<!--.+?-->", Pattern.DOTALL);

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = COMMENT_PATTERN.matcher(text);
        while (m.find()) {
            matches.add(FinderResult.of(m.start(), m.group()));
        }
        return matches;
    }
}
