package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class UrlFinder implements ExceptionMatchFinder {

    // Copied from pywikibot
    private static final String AT_END = "[^]\\s.:;,<>\"|)]";
    private static final String INSIDE = "[^]\\s<>\"|]";
    private static final String INSIDE_ESCAPED = "(" + INSIDE + "(?!&lt;))";
    private static final Pattern REGEX_URL = Pattern.compile("https?://" + INSIDE + "*" + AT_END);
    private static final Pattern REGEX_URL_ESCAPED =
            Pattern.compile("https?://" + INSIDE_ESCAPED + "*" + AT_END);

    private static final String REGEX_DOMAIN = "<L>+\\.(com|org|es|net|gov|edu|gob|info)";
    private static final RunAutomaton AUTOMATON_DOMAIN = new RunAutomaton(new RegExp(REGEX_DOMAIN).toAutomaton(new DatatypesAutomatonProvider()));

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        matches.addAll(RegExUtils.findMatches(text, isTextEscaped ? REGEX_URL_ESCAPED : REGEX_URL));
        matches.addAll(RegExUtils.findMatchesAutomaton(text, AUTOMATON_DOMAIN));
        return matches;
    }

}
