package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class XmlTagFinder implements ExceptionMatchFinder {

    // We want to avoid the XML comments to be captured by this
    private static final Pattern REGEX_XML_TAG = Pattern.compile("<[\\w/][^>]*+>");
    private static final Pattern REGEX_XML_TAG_ESCAPED = Pattern.compile("&lt;[\\w/].*?&gt;");

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, isTextEscaped ? REGEX_XML_TAG_ESCAPED : REGEX_XML_TAG);
    }

}
