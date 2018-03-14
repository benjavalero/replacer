package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class FileNameFinder implements ExceptionMatchFinder {

    private static final Pattern REGEX_FILE =
            Pattern.compile("File:[^]|]+?(?=]|\\||$)", Pattern.MULTILINE);
    private static final Pattern REGEX_FILE_VALUE =
            Pattern.compile("\\|[\\p{L}\\p{N}\\s-]+=[^}|]+\\.(?:svg|jpe?g|JPG|png|PNG|gif|ogg|pdf)\\b");

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, REGEX_FILE));
        matches.addAll(RegExUtils.findMatches(text, REGEX_FILE_VALUE));
        return matches;
    }

}
