package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class XmlTagFinder implements ExceptionMatchFinder {

    // We want to avoid the XML comments to be captured by this
    private static final String REGEX_XML_TAG = "<[\\w/][^>]*+>";
    private static final String REGEX_XML_TAG_ESCAPED = "&lt;[\\w/].*?&gt;";

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, isTextEscaped ? REGEX_XML_TAG_ESCAPED : REGEX_XML_TAG));
        return matches;
    }

}
