package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class CompleteTagFinder implements ExceptionMatchFinder {

    private static final Pattern REGEX_COMPLETE_TAG =
            Pattern.compile("<(math|source|syntaxhighlight).+?</\\1>", Pattern.DOTALL);
    private static final Pattern REGEX_COMPLETE_TAG_ESCAPED =
            Pattern.compile("&lt;(math|source|syntaxhighlight).+?&lt;/\\1&gt;", Pattern.DOTALL);

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, isTextEscaped ? REGEX_COMPLETE_TAG_ESCAPED : REGEX_COMPLETE_TAG);
    }

}
