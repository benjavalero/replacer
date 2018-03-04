package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileNameFinder implements ExceptionMatchFinder {

    private static final String REGEX_FILE_NAME = "(?<=[=|:])[^=|:]+\\.(?:svg|jpe?g|JPG|png|PNG|gif|ogg|pdf)";

    @Override
    public List<RegexMatch> findExceptionMatches(String text) {
        return RegExUtils.findMatches(text, REGEX_FILE_NAME);
    }

}
