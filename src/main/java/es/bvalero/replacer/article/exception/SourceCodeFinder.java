package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class SourceCodeFinder implements ErrorExceptionFinder {

    private static final String REGEX_TAG = "<(math|source)>.*?</\\1>";

    @Override
    public List<RegexMatch> findErrorExceptions(String text) {
        return RegExUtils.findMatches(text, REGEX_TAG, Pattern.DOTALL);
    }

}
