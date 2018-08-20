package es.bvalero.replacer.article.exception;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class CommentFinder implements ExceptionMatchFinder {

    // The nested regex takes twice more but it is worth as it captures completely the templates with inner templates
    // See the CompleteTagFinder to see how the alternative for the nested tags is implemented
    private static final RunAutomaton AUTOMATON_COMMENT_TAG =
        new RunAutomaton(new RegExp("\\<!--([^-]|-[^-]|--[^\\>])+--\\>").toAutomaton());

    private static final Pattern REGEX_COMMENT_TAG_ESCAPED =
            Pattern.compile("&lt;!--.+?--&gt;", Pattern.DOTALL);

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        if (isTextEscaped) {
            return RegExUtils.findMatches(text, REGEX_COMMENT_TAG_ESCAPED);
        } else {
            return RegExUtils.findMatchesAutomaton(text, AUTOMATON_COMMENT_TAG);
        }
    }

}
