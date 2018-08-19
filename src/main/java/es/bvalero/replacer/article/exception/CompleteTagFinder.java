package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class CompleteTagFinder implements ExceptionMatchFinder {

    private static final String TAG_NAMES = "(math|source|syntaxhighlight|blockquote)";
    private static final Pattern REGEX_COMPLETE_TAG =
            Pattern.compile("<" + TAG_NAMES + "[^>]*+>.+?</\\1>", Pattern.DOTALL);
    private static final Pattern REGEX_COMPLETE_TAG_ESCAPED =
            Pattern.compile("&lt;" + TAG_NAMES + ".+?&lt;/\\1&gt;", Pattern.DOTALL);

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, isTextEscaped ? REGEX_COMPLETE_TAG_ESCAPED : REGEX_COMPLETE_TAG);
    }

}
