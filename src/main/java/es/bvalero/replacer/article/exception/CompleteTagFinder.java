package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CompleteTagFinder implements ExceptionMatchFinder {

    // The gain on performance is so high (500% faster) using the text-directed version that it is better
    // to make different regex for each tag name.
    private static final String[] TAG_NAMES = {"math", "source", "syntaxhighlight", "blockquote"};
    private static final Pattern REGEX_COMPLETE_TAG_ESCAPED =
            Pattern.compile("&lt;(" + StringUtils.join(TAG_NAMES, "|") + ").+?&lt;/\\1&gt;", Pattern.DOTALL);

    private static final List<RunAutomaton> AUTOMATON_COMPLETE_TAGS = new ArrayList<>(TAG_NAMES.length);

    static {
        for (String tagName : TAG_NAMES) {
            // Build an alternation for the negation of the closing tag
            List<String> alternatives = new ArrayList<>(tagName.length() + 1);
            alternatives.add("\\<[^/]");
            for (int i = 0; i < tagName.length(); i++) {
                alternatives.add("\\</" + tagName.substring(0, i) + "[^" + tagName.substring(i, i + 1) + "]");
            }

            String regex = "\\<" + tagName + "([^\\<]|" + StringUtils.join(alternatives, "|") + ")+\\</" + tagName + "\\>";
            RunAutomaton automaton = new RunAutomaton(new RegExp(regex).toAutomaton());
            AUTOMATON_COMPLETE_TAGS.add(automaton);
        }
    }

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>();
        if (isTextEscaped) {
            matches.addAll(RegExUtils.findMatches(text, REGEX_COMPLETE_TAG_ESCAPED));
        } else {
            for (RunAutomaton automaton : AUTOMATON_COMPLETE_TAGS) {
                matches.addAll(RegExUtils.findMatchesAutomaton(text, automaton));
            }
        }
        return matches;
    }

}
