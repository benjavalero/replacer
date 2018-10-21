package es.bvalero.replacer.article.exception;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleReplacementFinder;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import es.bvalero.replacer.misspelling.MisspellingManager;
import es.bvalero.replacer.persistence.ReplacementType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProperNounFinder implements IgnoredReplacementFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_PROPER_NOUN = "(Domingo|Julio)<Z><Lu>";
    private static final RunAutomaton AUTOMATON_PROPER_NOUN =
            new RunAutomaton(new RegExp(REGEX_PROPER_NOUN).toAutomaton(new DatatypesAutomatonProvider()));

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_UPPERCASE = "\\p{Lu}";
    private static final Pattern PATTERN_UPPERCASE = Pattern.compile(REGEX_UPPERCASE);

    @Autowired
    private MisspellingManager misspellingManager;

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);

        // Person names. We don't need the extra letter captured for the surname.
        for (ArticleReplacement match : ArticleReplacementFinder.findReplacements(text, AUTOMATON_PROPER_NOUN, ReplacementType.IGNORED)) {
            matches.add(match.withText(match.getText().substring(0, match.getText().length() - 2)));
        }

        // Lowercase nouns that start with uppercase because after some special character
        // We don't need the extra letters captured for the separator
        for (ArticleReplacement match : ArticleReplacementFinder.findReplacements(text, misspellingManager.getUppercaseAutomaton(), ReplacementType.IGNORED)) {
            // Find the letter position
            Matcher m = PATTERN_UPPERCASE.matcher(match.getText());
            if (m.find()) {
                matches.add(match
                        .withStart(match.getStart() + m.start())
                        .withText(match.getText().substring(m.start())));
            }
        }

        return matches;
    }

}
