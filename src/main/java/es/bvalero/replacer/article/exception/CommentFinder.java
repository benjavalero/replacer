package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class CommentFinder implements ExceptionMatchFinder {

    private static final Pattern REGEX_COMMENT_TAG =
            Pattern.compile("<!--.+?-->", Pattern.DOTALL);
    private static final Pattern REGEX_COMMENT_TAG_ESCAPED =
            Pattern.compile("&lt;!--.+?--&gt;", Pattern.DOTALL);

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, isTextEscaped ? REGEX_COMMENT_TAG_ESCAPED : REGEX_COMMENT_TAG);
    }

}
