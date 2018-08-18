package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class QuotesFinder implements ExceptionMatchFinder {

    private static final Pattern REGEX_SINGLE_QUOTES = Pattern.compile("('{2,5}).+?[^']\\1(?!')");
    private static final Pattern REGEX_SINGLE_QUOTES_ESCAPED =
            Pattern.compile("((&apos;){2,5}).+?(?<!&apos;)\\1(?!&apos;)");

    private static final RunAutomaton AUTOMATON_ANGULAR_QUOTES =
            new RunAutomaton(new RegExp("«[^»]++»").toAutomaton());

    private static final RunAutomaton AUTOMATON_TYPOGRAPHIC_QUOTES =
            new RunAutomaton(new RegExp("“[^”]++”").toAutomaton());

    // For the automaton the quote needs an extra backslash
    private static final RunAutomaton AUTOMATON_DOUBLE_QUOTES =
            new RunAutomaton(new RegExp("\\\"[^\\\"\n]+\\\"").toAutomaton());
    private static final Pattern REGEX_DOUBLE_QUOTES_ESCAPED = Pattern.compile("&quot;.+?&quot;");

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        if (isTextEscaped) {
            // There are lots of lines with single quotes not closed
            matches.addAll(RegExUtils.findMatches(text, REGEX_SINGLE_QUOTES_ESCAPED));
            matches.addAll(RegExUtils.findMatches(text, REGEX_DOUBLE_QUOTES_ESCAPED));
        } else {
            matches.addAll(RegExUtils.findMatches(text, REGEX_SINGLE_QUOTES));
            matches.addAll(RegExUtils.findMatchesAutomaton(text, AUTOMATON_DOUBLE_QUOTES));
        }
        matches.addAll(RegExUtils.findMatchesAutomaton(text, AUTOMATON_ANGULAR_QUOTES));
        matches.addAll(RegExUtils.findMatchesAutomaton(text, AUTOMATON_TYPOGRAPHIC_QUOTES));
        return matches;
    }

}
