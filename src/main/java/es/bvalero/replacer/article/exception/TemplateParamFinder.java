package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TemplateParamFinder implements ErrorExceptionFinder {

    private static final String REGEX_TEMPLATE_PARAM = "\\|[\\wÁáÉéÍíÓóÚúÜüÑñ\\-\\s]+=";

    @Override
    public List<RegexMatch> findErrorExceptions(String text) {
        return RegExUtils.findMatches(text, REGEX_TEMPLATE_PARAM);
    }

}
