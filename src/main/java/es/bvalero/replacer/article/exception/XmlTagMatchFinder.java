package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class XmlTagMatchFinder implements ExceptionMatchFinder {

    // We want to avoid the XML comments to be captured by this
    private static final String REGEX_XML_TAG = "<[\\wÁáÉéÍíÓóÚúÜüÑñ\\-\\s=\"/]+>";
    private static final String REGEX_XML_TAG_ESCAPED = "&lt;[\\wÁáÉéÍíÓóÚúÜüÑñ\\-\\s=\"/]+&gt;";

    @Override
    public List<RegexMatch> findExceptionMatches(String text) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, REGEX_XML_TAG));
        matches.addAll(RegExUtils.findMatches(text, REGEX_XML_TAG_ESCAPED));
        return matches;
    }

}
