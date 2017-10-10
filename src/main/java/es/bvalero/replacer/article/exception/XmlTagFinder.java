package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class XmlTagFinder implements ErrorExceptionFinder {

    // This regex already covers HTML comments
    private static final String REGEX_XML_TAG = "<[^>]+>";
    private static final String REGEX_XML_TAG_ESCAPED = "&lt;.+?&gt;";

    @Override
    public List<RegexMatch> findErrorExceptions(String text) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, REGEX_XML_TAG));
        matches.addAll(RegExUtils.findMatches(text, REGEX_XML_TAG_ESCAPED, Pattern.DOTALL));
        return matches;
    }

}
