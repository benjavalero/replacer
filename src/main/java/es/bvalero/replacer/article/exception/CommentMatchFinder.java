package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CommentMatchFinder implements ExceptionMatchFinder {

    private static final String REGEX_COMMENT_TAG = "<!--.+?-->";
    private static final String REGEX_COMMENT_TAG_ESCAPED = "&lt;!--.+?--&gt;";

    @Override
    public List<RegexMatch> findExceptionMatches(String text) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, REGEX_COMMENT_TAG, Pattern.DOTALL));
        matches.addAll(RegExUtils.findMatches(text, REGEX_COMMENT_TAG_ESCAPED, Pattern.DOTALL));
        return matches;
    }

}
