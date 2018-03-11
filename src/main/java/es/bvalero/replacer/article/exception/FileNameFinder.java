package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileNameFinder implements ExceptionMatchFinder {

    private static final String REGEX_FILE_NAME = "(?:File:|=|\\|)[^]}|=]+?\\.\\w{3,4}";

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        return RegExUtils.findMatches(text, REGEX_FILE_NAME);
    }

}
