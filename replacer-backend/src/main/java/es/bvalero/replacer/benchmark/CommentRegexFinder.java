package es.bvalero.replacer.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CommentRegexFinder extends CommentAbstractFinder {

    private static final Pattern COMMENT_PATTERN = Pattern.compile("<!--.+?-->", Pattern.DOTALL);

    Set<IgnoredReplacement> findMatches(String text) {
        Set<IgnoredReplacement> matches = new HashSet<>();
        Matcher m = COMMENT_PATTERN.matcher(text);
        while (m.find()) {
            matches.add(IgnoredReplacement.of(m.start(), m.group()));
        }
        return matches;
    }

}
