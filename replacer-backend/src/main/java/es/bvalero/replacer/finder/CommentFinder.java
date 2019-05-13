package es.bvalero.replacer.finder;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
class CommentFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_COMMENT_TAG = "<!--.+?-->";
    private static final Pattern PATTERN_COMMENT_TAG = Pattern.compile(REGEX_COMMENT_TAG, Pattern.DOTALL);

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        return findMatchResults(text, PATTERN_COMMENT_TAG);
    }

}
