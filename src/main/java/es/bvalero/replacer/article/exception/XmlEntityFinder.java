package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class XmlEntityFinder implements ExceptionMatchFinder {

    private static final String REGEX_XML_ENTITY = "&[a-z]++;";

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        if (isTextEscaped) {
            matches.addAll(RegExUtils.findMatches(text, REGEX_XML_ENTITY));
        }
        return matches;
    }

}
