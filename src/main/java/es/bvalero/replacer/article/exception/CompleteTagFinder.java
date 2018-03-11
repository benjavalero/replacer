package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CompleteTagFinder implements ExceptionMatchFinder {

    private static final String REGEX_COMPLETE_TAG = "<(math|source|syntaxhighlight).+?</\\1>";
    private static final String REGEX_COMPLETE_TAG_ESCAPED = "&lt;(math|source|syntaxhighlight).+?&lt;/\\1&gt;";

    @Override
    public List<RegexMatch> findExceptionMatches(String text) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, REGEX_COMPLETE_TAG, Pattern.DOTALL));
        matches.addAll(RegExUtils.findMatches(text, REGEX_COMPLETE_TAG_ESCAPED, Pattern.DOTALL));
        return matches;
    }

}
