package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.misspelling.MisspellingManager;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProperNounFinder implements ExceptionMatchFinder {

    private static final RunAutomaton AUTOMATON_PROPER_NOUN =
            new RunAutomaton(new RegExp("(Domingo|Julio)<Z><Lu>").toAutomaton(new DatatypesAutomatonProvider()));

    private static final Pattern REGEX_UPPERCASE = Pattern.compile("\\p{Lu}");

    @Autowired
    private MisspellingManager misspellingManager;

    @Override
    public List<RegexMatch> findExceptionMatches(String text, boolean isTextEscaped) {
        List<RegexMatch> matches = new ArrayList<>(100);

        // Person names. We don't need the extra letter captured for the surname.
        for (RegexMatch match : RegExUtils.findMatchesAutomaton(text, AUTOMATON_PROPER_NOUN)) {
            match.setOriginalText(match.getOriginalText().substring(0, match.getOriginalText().length() - 2));
            matches.add(match);
        }

        // Lowercase nouns. We don't need the extra letters captured for the separator.
        for (RegexMatch match : RegExUtils.findMatchesAutomaton(text, misspellingManager.getUppercaseMisspellingsAutomaton())) {
            // Find the letter position
            Matcher m = REGEX_UPPERCASE.matcher(match.getOriginalText());
            if (m.find()) {
                match.setPosition(match.getPosition() - m.start());
                match.setOriginalText(match.getOriginalText().substring(m.start()));
                matches.add(match);
            }
        }

        return matches;
    }

}
