package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class TemplateParamFinder implements ExceptionMatchFinder {

    private static final Pattern REGEX_TEMPLATE_PARAM = Pattern.compile("\\|[\\p{L}\\p{N}\\s-_]++(?==)");

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, REGEX_TEMPLATE_PARAM);
    }

}
