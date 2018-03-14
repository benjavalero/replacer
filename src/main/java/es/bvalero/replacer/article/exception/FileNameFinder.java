package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class FileNameFinder implements ExceptionMatchFinder {

    private static final String REGEX_FILE = "File:[^]|]+?(?=\\]|\\||$)";
    private static final String REGEX_FILE_VALUE = "\\|[\\p{L}\\p{N}\\s-]+=[^}|]+\\.(?:svg|jpe?g|JPG|png|PNG|gif|ogg|pdf)\\b";

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, REGEX_FILE, Pattern.MULTILINE));
        matches.addAll(RegExUtils.findMatches(text, REGEX_FILE_VALUE));
        return matches;
    }

}
